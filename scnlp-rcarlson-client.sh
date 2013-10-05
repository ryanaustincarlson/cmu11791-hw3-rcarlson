cd deploy
time runRemoteAsyncAE.sh tcp://mu.lti.cs.cmu.edu:61616 ScnlpQueue -d ../hw3-rcarlson/src/main/resources/scnlp-rcarlson-client.xml -c ../hw3-rcarlson/src/main/resources/collection_readers/FileSystemCollectionReader.xml -o output
