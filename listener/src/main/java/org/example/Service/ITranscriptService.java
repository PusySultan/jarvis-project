package org.example.Service;

public interface ITranscriptService
{
    boolean isEndCMD(byte[] audioData);
    String getCMD();
}
