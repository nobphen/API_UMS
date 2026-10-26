package com.ums.common.numbering;

public interface DocumentNumberService {

    /**
     * The next number for this kind of document, e.g. INV-2026-00015. Never the same twice, even when
     * many users save at the same moment. Call it inside the transaction that saves the document:
     * if that transaction rolls back, the number is given back too.
     */
    String next(DocumentType type);
}
