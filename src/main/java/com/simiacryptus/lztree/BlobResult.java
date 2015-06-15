package com.simiacryptus.lztree;

public class BlobResult {
  public final Blob data;
  public final BlobId id;
  public BlobResult(Blob data, BlobId id) {
    super();
    this.data = data;
    this.id = id;
  }
}