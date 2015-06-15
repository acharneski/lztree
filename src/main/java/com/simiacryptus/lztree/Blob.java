package com.simiacryptus.lztree;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Blob {
  
  public final int size;
  public final byte[] data;
  public final Blob parent;
  
  public Blob(byte[] rawData) {
    this(null, rawData);
  }
  
  public Blob(Blob parent, byte[] rawData) {
    Deflater compressor = new Deflater();
    this.size = rawData.length;
    // compressor.setLevel(1);
    // compressor.deflate(new byte[]{});
    if (null != parent)
    {
      compressor.setDictionary(parent.getPathData());
    }
    compressor.setInput(rawData);
    compressor.finish();
    byte[] b = new byte[rawData.length];
    this.data = Arrays.copyOf(b, compressor.deflate(b));
    this.parent = parent;
  }
  
  public Blob encode(byte[] rawData) {
    return new Blob(this, rawData);
  }
  
  public SoftReference<byte[]> rawCache = null;
  
  public synchronized byte[] getPathData() {
    return parent == null ? getRawData() : concat(parent.getPathData(), getRawData());
  }
  
  private byte[] concat(byte[] a, byte[] b) {
    byte[] c = new byte[a.length + b.length];
    for (int i = 0; i < a.length; i++)
      c[i] = a[i];
    for (int i = 0; i < b.length; i++)
      c[a.length + i] = b[i];
    return c;
  }
  
  public synchronized byte[] getRawData() {
    byte[] cached = null == rawCache ? null : rawCache.get();
    if (null != cached) return cached;
    Inflater decompressor = new Inflater();
    decompressor.setInput(data);
    byte[] bytes = new byte[size];
    try {
      if (null != parent)
      {
        if (0 < decompressor.inflate(bytes)) throw new RuntimeException();
        assert (decompressor.needsDictionary());
        decompressor.setDictionary(parent.getPathData());
      }
      decompressor.inflate(bytes);
    } catch (DataFormatException e) {
      throw new RuntimeException(e);
    }
    decompressor.end();
    rawCache = new SoftReference<byte[]>(bytes);
    return bytes;
  }
  
}