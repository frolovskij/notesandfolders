package com.notesandfolders;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import java.io.*;
import java.util.Date;

public class Main {

  public static void main(String[] args) throws Exception {
    String backupFileName = args[0];
    String password = args[1];

    File backupFile = new File(args[0]);
    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(backupFile)));

    final String passwordSha1 = dis.readUTF();
    final String encryptedKey = dis.readUTF();
    final String key = SimpleCrypto.decrypt(passwordSha1, encryptedKey);

    while (dis.available() != 0) {
      int dataLen = dis.readInt();
      if (dataLen > 0) {
        byte[] data = new byte[dataLen];
        dis.read(data);
        Node node = getNodeFromByteArray(data);
        System.out.println(node.getName());
      }
    }

  }

  public static Node getNodeFromByteArray(byte[] data) {
    if (data == null) {
      return null;
    }

    Node n = new Node();

    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(data, 0,
          data.length);
      DataInputStream dis = new DataInputStream(bais);

      n.setId(dis.readLong());
      n.setParentId(dis.readLong());
      n.setName(dis.readUTF());

      final int tcLength = dis.readInt();
      byte[] tc = new byte[tcLength];
      dis.read(tc);
      n.setTextContent(SimpleCrypto.toHex(tc));
      n.setEncryptVersion(1);

      n.setDateCreated(new Date(dis.readLong()));
      n.setDateModified(new Date(dis.readLong()));
      n.setType(NodeType.getByOrdinal(dis.readInt()));

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return n;
  }

}
