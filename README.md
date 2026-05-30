# Mini-DBMS-Engine
A mini Database Management System built in Java from scratch, implementing core DBMS concepts across three milestones.
## Milestones
MS1 — Core Engine

Page-based persistent storage with configurable page size
Record insertion and multi-mode querying (full scan, conditional, and pointer-based)
Operation tracing with execution time logging

MS2 — Bitmap Index & Recovery

Bitmap Index with dynamic updates on every insertion
Multi-column index-based selection using bitwise AND with fallback linear scan
Trace-driven data recovery that reconstructs missing pages in their original positions

MS3 — Dense Index

Dense Index with sorted key/pointer pairs stored across configurable block sizes
Each pointer encodes the record's page number and row position

## Tech

Java
Object serialization for disk persistence

## Structure

DBApp.java — DBMS-level operations
Table.java — Table-level logic
Page.java — Page-level storage
DenseIndexBlock.java — Dense index block structure
FileManager.java — Disk I/O and serialization
