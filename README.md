# lztree
Demonstration of an adaptive data compression and storage scheme

## Background

The LZ family of compression algorithms, including the jdk's zip [Inflater](http://docs.oracle.com/javase/7/docs/api/java/util/zip/Inflater.html)/Deflater classes (which end up using [DEFLATE](https://en.wikipedia.org/wiki/DEFLATE)), are a widely used data compression technology. It is widely used to compress files and data over network connections. 

LZ-type compressors, including the jdk's DEFLATE implementation, allow the caller to specify the intial "dictionary" string of a compressor. Matching substrings between this dictionary and the data to be compressed yield compressed sections of output, boosting performance sometimes considerably. This been notably used in Google's SPDY protocol to speed up http requests.

A valuble similar structure to what we are developing is a git repository. Stated simply, a git repository stores a collection of files and the patches applied to them over time. Theoretically, a git may have 1000 files and 1000 revisions, meaning it "contains" 1000000 blobs if you fully expanded the addressable file space, but it is also extremely redundant or symetric. E.g. 99% of the blobs are duplicates of the remaining 1%; of the 1%, each revision is 95% unchanged on average from the prior version; there are also considerable similarities between files; etc. Git implements an efficient storage engine for code simply by compressing a concatenated list of patchfiles, though this storage model requires/assumes that new blobs are known to already be based on minor changes from known preceeding blobs.

## Introduction

Our service seeks to extend this technology to implement a random-access blob storage service that not only compresses the data blobs individually, but also compresses them based off differences/similarities to existing blobs in the database. These relations are determined at runtime without any given assumptions about use. We implement this by creating a tree structure, where blobs are stored in lists at both leaf and intermediate nodes, and where each node's path defines the initial dictionary of the lz compression phase.

## Running it

https://github.com/acharneski/lztree/blob/ccf1b6374dfd30ce0962da3255585aa291f8173e/src/test/java/com/simiacryptus/lztree/Main.java#L44

## Data Sets

Due to the wide availibility and similar nature to what we are building here, I have found existing git repositories from well-known projects to be good data sources for development. Additionally, there is test support for loading a [wikipedia gzipped xml dump file](https://github.com/acharneski/lztree/blob/ccf1b6374dfd30ce0962da3255585aa291f8173e/src/test/java/com/simiacryptus/lztree/Main.java#L85) to load data with a less redundant nature.

1. https://github.com/torvalds/linux.git - A well-known git repo, about 1GB
2. https://github.com/apache/spark - Another repo, quite active, about 100MB
3. https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2
