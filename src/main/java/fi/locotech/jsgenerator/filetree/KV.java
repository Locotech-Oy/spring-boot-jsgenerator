package fi.locotech.jsgenerator.filetree;

import lombok.Data;

@Data
public class KV<K, V> {
  private K k;
  private V v;

  public KV(K k, V v) {
    this.k = k;
    this.v = v;
  }
}
