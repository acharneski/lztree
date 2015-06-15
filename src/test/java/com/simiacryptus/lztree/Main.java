package com.simiacryptus.lztree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Test;

public class Main {
  
  int totalRawSize = 0;
  int totalSoloSize = 0;
  int totalFinalSize = 0;
  int numberOfItems = 0;
  long startTime = System.currentTimeMillis();
  CompressTree db = new CompressTree();
  
  @SuppressWarnings("resource")
  @Test
  public void git() throws IOException, NoHeadException, GitAPIException{
    
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    Repository repository = builder.setGitDir(new File("L:/code/linux/.git"))
      .readEnvironment() // scan environment GIT_* variables
      .findGitDir() // scan up the file system tree
      .build();
    RevWalk walk = new RevWalk(repository);
    StreamSupport.stream(new Git(repository).log().call().spliterator(), false).forEach(head->{
      try {
        
        RevTree tree = walk.parseCommit(head.getId()).getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        //treeWalk.setFilter(PathFilter.create("/"));
        while(treeWalk.next())
        {
          ObjectId objectId = treeWalk.getObjectId(0);
          if(treeWalk.getPathString().endsWith(".jar")) continue;
          ObjectLoader loader;
          try {
            loader = repository.open(objectId);
          } catch (Exception e) {
            Util.log(e.getMessage());
            continue;
          }
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          loader.copyTo(buf);
          String text = new String(buf.toByteArray(), Charset.forName("UTF-8"));
          feed(head.toString() + "/" + treeWalk.getPathString(), text);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
  
  
  @SuppressWarnings("unchecked")
  @Test
  public void wikipedia() throws IOException, URISyntaxException, XMLStreamException, FactoryConfigurationError {
    final String wikiInput = "L:/enwiki-latest-pages-articles.xml.bz2";
    
    XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(
        new BZip2CompressorInputStream(new FileInputStream(wikiInput)));
    reader.forEachRemaining(new Consumer<XMLEvent>() {
      Stack<String> path = new Stack<String>();
      String currentTitle = null; 
      String text = ""; 
      @Override
      public void accept(XMLEvent t) {
        if (t.isStartElement()) {
          path.push(t.asStartElement().getName().getLocalPart());
        } else if (t.isEndElement()){
          path.pop();
        }
        if(t.isCharacters()) {
          if("[mediawiki, page, title]".equals(path.toString()))
          {
            if(!text.isEmpty())
            {
              Main.this.feed(currentTitle, text);
            }            
            text = "";
            currentTitle = t.toString();
          } else if("[mediawiki, page, revision, text]".equals(path.toString()))
          {
            text += t.toString();
          }
        }
      }
    });
  }

  private BlobId feed(String title, String text) {
    Blob input = new Blob(text.getBytes());
    BlobResult output = db.store(input);
    
    {
      double a = input.size;
      totalRawSize += a;
      double b = input.data.length;
      totalSoloSize += b;
      double c = output.data.data.length;
      totalFinalSize += c;
      Util.log("Stored %s as [%s]: %s -(%.03f)-> %s -(%.03f)-> %s", title, output.id, a, b*100./a, b, c*100./b, c);
    }
    
    {
      double a = totalRawSize;
      double b = totalSoloSize;
      double c = totalFinalSize;
      Util.log("Total stats: %s items in %.3f sec: %s -(%.03f)-> %s -(%.03f)-> %s", numberOfItems++, (System.currentTimeMillis()-startTime)/1000., a, b*100./a, b, c*100./b, c);
    }
    
    Assert.assertEquals(output.data, db.get(output.id));
    
    return output.id;
  }
}
