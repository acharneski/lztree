package com.simiacryptus.lztree;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BlobId {
  public final List<Integer> treePath;
  public final int id;
  public BlobId(int[] treePath, int id) {
    super();
    this.treePath = IntStream.of(treePath).boxed().collect(Collectors.toList());
    this.id = id;
  }
  
  @Override
  public String toString() {
    return treePath.stream().map(i->i.toString()).reduce((a,b)->a+"-"+b).orElse("") + "#" + id;
  }
  
  
}