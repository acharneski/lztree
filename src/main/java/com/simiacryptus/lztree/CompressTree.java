package com.simiacryptus.lztree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class CompressTree
{
  public final int[] label;
  public final CompressTree parent;
  public final LinkedHashMap<Blob, CompressTree> children = new LinkedHashMap<Blob, CompressTree>();
  public final List<Blob> datastore = new ArrayList<Blob>();
  public int childCounter = 0;
  public boolean enabled = true;
  private int age = 0;
  
  public CompressTree() {
    this(new int[]{}, null);
  }
  
  public CompressTree(int[] label, CompressTree parent) {
    super();
    this.label = label;
    this.parent = parent;
  }
  
  public Blob get(BlobId id) {
    if(Arrays.equals(id.treePath.stream().mapToInt(i->i).toArray(), this.label))
    {
      return datastore.get(id.id);
    }
    else
    {
      CompressTree child = children.values().stream().skip(id.treePath.get(label.length)).findFirst().orElse(null);
      return child.get(id);
    }
  }
  
  public BlobResult store(Blob blob) {
    setAge(getAge() + 1);
    Blob highestCompression = children.keySet().stream()
        .filter(x->children.get(x).enabled)
        .map(x -> new Blob(x, blob.getRawData()))
        .sorted(Comparator.comparing(b -> b.data.length))
        .findFirst().orElse(null);
    int delta = null == highestCompression ? -1 : (blob.data.length - highestCompression.data.length);
    int minBytes = 8;
    double minCompression = .03;
    if(childCount() > 16) minCompression = 0.;
    else if(childCount() > 8) minCompression = 0.01;
    if (delta > (minCompression * blob.size) || delta > minBytes)
    {
      CompressTree child = children.get(highestCompression.parent);
      return child.store(highestCompression);
    }
    else
    {
      children.values().stream()
        .filter(e -> e.age < age - 100 && e.datastore.size() == 0)
        .forEach(e -> e.enabled = false);
      if (blob.data.length > 50 && childCount() < 32) {
        // log("Adding new compression tree node, new size: %s", children.size());
        int[] newPath = Arrays.copyOf(label, label.length+1);
        newPath[newPath.length-1] = childCounter++;
        children.put(blob, new CompressTree(newPath, this).setAge(age));
      }
      int id = datastore.size();
      datastore.add(blob);
      return new BlobResult(blob, new BlobId(label, id));
    }
  }

  private int childCount() {
    int childCount = (int) children.keySet().stream().filter(x->children.get(x).enabled).count();
    return childCount;
  }
  
  public int getAge() {
    return age;
  }
  
  public CompressTree setAge(int age) {
    this.age = age;
    return this;
  }
}