# 字段加密支持搜索技术方案

> 为了数据安全我们在开发过程中经常会对重要的数据进行加密存储，常见的有：密码、手机号、电话号码、详细地址、银行卡号、信用卡验证码等信息，这些信息对加解密的要求也不一样，比如说密码我们需要加密存储，一般使用的 AES 、DES 、或者不可逆的慢 hash 算法(慢 hash严格来说不叫加密，因为加密要求可逆)，在检索时我们既不需要解密也不需要模糊查找，直接使用密文完全匹配，但是手机号就不能这样做，因为手机号我们要查看原信息，并且对手机号还需要支持模糊查找，下面是三种可落地的针对可逆加解密的数据支持模糊查询的实现方式。

# 同态加密方案
同态加密方案是一种特殊的加密技术，允许在加密状态下进行计算，而不需要暴露明文数据。在同态加密方案中，加密的数据可以进行加法和乘法等计算操作，同时保持数据的加密状态。以下是两种常见的同态加密方案：

1. Paillier同态加密方案：这种方案基于公钥加密算法，可以支持加法和乘法计算操作。在这种方案中，数据被加密，并使用公钥加密算法生成一个密文。对于密文的加法计算，可以使用同态性质直接对加密数据进行加法操作，得到一个新的密文。对于密文的乘法计算，需要使用一种特殊的协议，例如Garbled Circuit协议，来计算加密数据的乘积。

2. Fully Homomorphic Encryption (FHE)方案：这种方案可以支持任意计算操作，包括加法、乘法、比较、排序等。FHE方案通常使用高度优化的加密算法和计算技术，例如Lattice-based加密算法和Gentry的Bootstrapping技术。FHE方案的计算效率较低，但可以实现高度安全的计算。

# 可搜索加密方案
可搜索加密方案是一种安全的加密技术，允许在加密的数据集中进行搜索和匹配，同时保护数据的隐私性。在可搜索加密方案中，搜索关键字被加密，然后与加密的数据进行比较，以确定是否匹配。以下是一些常见的可搜索加密方案：

1. 基于单词加密的方案：这种方案用于对文本数据进行加密搜索。在这种方案中，文本数据被分解成单词，然后对每个单词进行加密。搜索时，搜索关键字也被分解成单词，并对每个单词进行加密。然后，加密的关键字与加密的单词进行比较，以确定是否匹配。

2. 基于布隆过滤器的方案：这种方案使用布隆过滤器对加密的数据进行索引。布隆过滤器是一种高效的数据结构，可以用于检查一个元素是否属于一个集合中。在这种方案中，搜索关键字被加密，并用于查询布隆过滤器。如果布隆过滤器返回一个匹配，那么就可以在相应的数据项中进一步查找。

3. 基于密文索引的方案：这种方案使用密文索引来存储加密的数据。密文索引是一种数据结构，可以用于存储加密的数据，并支持在加密状态下进行搜索和匹配。在这种方案中，搜索关键字被加密，并与密文索引进行比较，以确定是否匹配。


## 基于数据库特性加解密
在数据库中实现与程序一致的加解密算法，修改模糊查询条件，使用数据库加解密函数先解密再模糊查找，这样做的优点是实现成本较低，只需要将以往的模糊查找稍微修改一下就可以实现，但是缺点也很明显，这样做无法利用数据库的索引来优化查询，甚至有一些数据库可能无法保证与程序实现一致的加解密算法，但是对于常规的加解密算法都可以保证与应用程序一致。
如果对查询性能要求不是特别高、对数据安全性要求一般，可以使用常见的加解密算法比如说AES、DES之类的也是一个不错的选择。
参考资料：
[https://www.jianshu.com/p/b11816770d46](https://www.jianshu.com/p/b11816770d46)
[https://github.com/digoal/blog/blob/master/201710/20171020_01.md](https://github.com/digoal/blog/blob/master/201710/20171020_01.md)
缺点：
性能较差，对于 pg 和 mysql 不同的数据库单独开发，对 ES 无法支持

## 基于单词加密的方案
对密文数据进行分词组合，将分词组合的结果集分别进行加密，然后存储到扩展列，查询时通过key like '%partial%'，这是一个比较划算的实现方法，我们先来分析一下它的实现思路。
先对字符进行固定长度的分组，将一个字段拆分为多个，比如说根据 4 位英文字符（半角），2 个中文字符（全角）为一个检索条件，举个例子：
ningyu1使用4个字符为一组的加密方式，第一组ning ，第二组ingy ，第三组ngyu ，第四组gyu1 … 依次类推。
如果需要检索所有包含检索条件 4 个字符的数据比如：ingy ，加密字符后通过 key like “%partial%” 查库。
我们都知道加密后长度会增长，增长的这部分长度存储就是我们要花费的额外成本，典型的使用成本来换取速度，密文增长的幅度随着算法不同而不同以DES举例，13800138000加密前占11个字节，加密后的串HE9T75xNx6c5yLmS5l4r6Q==占24个字节，增长是2.18倍
这个方法虽然可以实现加密数据的模糊查询，但是对模糊查询的字符长度是有要求的，以我上面举的例子模糊查询字符原文长度必须大于等于 4 个英文/数字，或者 2 个汉字，再短的长度不建议支持，因为分词组合会增多从而导致存储的成本增加，反而安全性降低。
![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/fdf72cbde33535943445735dafc23dde.png)
示例代码

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordEncryption {
    public static void main(String[] args) {
        // Example text data to be encrypted
        String plaintext = "This is a test text.";
        
        // Split the plaintext into words
        String[] words = plaintext.split(" ");
        
        // Generate a secret key and a public key
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublicKey();
        SecretKey secretKey = keyPair.getSecretKey();
        
        // Encrypt each word using the public key
        List<byte[]> encryptedWords = new ArrayList<>();
        for (String word : words) {
            byte[] encryptedWord = encrypt(word.getBytes(), publicKey);
            encryptedWords.add(encryptedWord);
        }
        
        // Search for a keyword in the encrypted words
        String keyword = "test";
        byte[] encryptedKeyword = encrypt(keyword.getBytes(), publicKey);
        for (int i = 0; i < encryptedWords.size(); i++) {
            byte[] encryptedWord = encryptedWords.get(i);
            if (Arrays.equals(encryptedWord, encryptedKeyword)) {
                System.out.println("Found keyword \"" + keyword + "\" at index " + i);
            }
        }
        
        // Decrypt a word using the secret key
        byte[] encryptedWord = encryptedWords.get(0);
        byte[] decryptedWord = decrypt(encryptedWord, secretKey);
        String plaintextWord = new String(decryptedWord);
        System.out.println("Decrypted word: " + plaintextWord);
    }
    
    // Generate a public-private key pair
    private static KeyPair generateKeyPair() {
        // TODO: Implement key pair generation using a suitable algorithm
        return null;
    }
    
    // Encrypt a plaintext message using a public key
    private static byte[] encrypt(byte[] plaintext, PublicKey publicKey) {
        // TODO: Implement encryption using a suitable algorithm
        return null;
    }
    
    // Decrypt a ciphertext message using a secret key
    private static byte[] decrypt(byte[] ciphertext, SecretKey secretKey) {
        // TODO: Implement decryption using a suitable algorithm
        return null;
    }
}

// Key pair class for holding public and secret keys
class KeyPair {
    private PublicKey publicKey;
    private SecretKey secretKey;
    
    public KeyPair(PublicKey publicKey, SecretKey secretKey) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public SecretKey getSecretKey() {
        return secretKey;
    }
}

// Public key class for encryption
class PublicKey {
    // TODO: Implement public key data and methods
}

// Secret key class for decryption
class SecretKey {
    // TODO: Implement secret key data and methods
}

```

参考资料：
[https://open.taobao.com/docV3.htm?docId=106213&docType=1](https://open.taobao.com/docV3.htm?docId=106213&docType=1)
缺点：
1. 对不同类型的字段要不同处理，比如纯数字、纯英文、中文
2. 不够灵活，英文只能模糊检索四位，中文只能模糊检索两位
3. 用于检索的密文扩展字段占用空间较多


## 基于布隆过滤器的方案
这种方案使用布隆过滤器对加密的数据进行索引。布隆过滤器是一种高效的数据结构，可以用于检查一个元素是否属于一个集合中。在这种方案中，搜索关键字被加密，并用于查询布隆过滤器。如果布隆过滤器返回一个匹配，那么就可以在相应的数据项中进一步查找。
示例代码

```java
import java.util.BitSet;

public class BloomFilterEncryption {
    public static void main(String[] args) {
        // Example text data to be encrypted
        String plaintext = "This is a test text.";
        
        // Generate a secret key and a public key
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublicKey();
        SecretKey secretKey = keyPair.getSecretKey();
        
        // Build a Bloom filter index of the encrypted words
        int numWords = 1000; // number of words to index
        int numBits = 10000; // number of bits in the Bloom filter
        int numHashes = 3; // number of hash functions to use
        BloomFilter bloomFilter = new BloomFilter(numBits, numHashes);
        for (int i = 0; i < numWords; i++) {
            // Generate a random word to encrypt
            String word = generateRandomWord();
            
            // Encrypt the word using the public key
            byte[] encryptedWord = encrypt(word.getBytes(), publicKey);
            
            // Add the encrypted word to the Bloom filter
            bloomFilter.add(encryptedWord);
        }
        
        // Search for a keyword in the Bloom filter
        String keyword = "test";
        byte[] encryptedKeyword = encrypt(keyword.getBytes(), publicKey);
        boolean exists = bloomFilter.contains(encryptedKeyword);
        if (exists) {
            System.out.println("Found keyword \"" + keyword + "\" in the Bloom filter.");
        } else {
            System.out.println("Keyword \"" + keyword + "\" not found in the Bloom filter.");
        }
    }
    
    // Generate a public-private key pair
    private static KeyPair generateKeyPair() {
        // TODO: Implement key pair generation using a suitable algorithm
        return null;
    }
    
    // Generate a random word
    private static String generateRandomWord() {
        // TODO: Implement random word generation
        return null;
    }
    
    // Encrypt a plaintext message using a public key
    private static byte[] encrypt(byte[] plaintext, PublicKey publicKey) {
        // TODO: Implement encryption using a suitable algorithm
        return null;
    }
    
    // Decrypt a ciphertext message using a secret key
    private static byte[] decrypt(byte[] ciphertext, SecretKey secretKey) {
        // TODO: Implement decryption using a suitable algorithm
        return null;
    }
}

// Key pair class for holding public and secret keys
class KeyPair {
    private PublicKey publicKey;
    private SecretKey secretKey;
    
    public KeyPair(PublicKey publicKey, SecretKey secretKey) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public SecretKey getSecretKey() {
        return secretKey;
    }
}

// Public key class for encryption
class PublicKey {
    // TODO: Implement public key data and methods
}

// Secret key class for decryption
class SecretKey {
    // TODO: Implement secret key data and methods
}

// Bloom filter class for indexing encrypted words
class BloomFilter {
    private BitSet bitset;
    private int numHashes;
    
    public BloomFilter(int numBits, int numHashes) {
        this.bitset = new BitSet(numBits);
        this.numHashes = numHashes;
    }
    
    public void add(byte[] data) {
    for (int i = 0; i < numHashes; i++) {
        int hash = hash(data, i);
        bitset.set(hash, true);
    }
}

public boolean contains(byte[] data) {
    for (int i = 0; i < numHashes; i++) {
        int hash = hash(data, i);
        if (!bitset.get(hash)) {
            return false;
        }
    }
    return true;
}

private int hash(byte[] data, int index) {
    // TODO: Implement a suitable hash function
    return 0;
}
}
```
## 基于 Hash 前缀索引的加解密方案（推荐）
用于检索的密文扩展字段生成方式不同：
1. 遍历字段明文每个字符
2. 对字符进行 Hash 算法后取前三位
3. 遍历完成后得到长度为「字符个数 * 3」的检索扩展字段

当检索时：
1. 遍历检索数据每个字符
2. 对字段进行 Hash 算法后取前三位
3. 遍历完成后得到长度为「检索字符个数 * 3」的 Hash 检索字段
4. 进行数据库的模糊匹配 like "% Hash 检索字段 %"


### 示例代码
1. **encryptField** 方法用于对字段进行加密，遍历字段明文的每个字符，对字符进行 Hash 算法加密后取前三位，遍历完成后得到长度为「字符个数 * 3」的密文扩展字段。

2. **encryptKeyword** 方法用于对检索条件进行加密，遍历检索数据的每个字符，对字符进行 Hash 算法加密后取前三位，遍历完成后得到长度为「检索字符个数 * 3」的加密检索字段。

3. **fuzzyMatch** 方法用于执行模糊匹配，接收一个加密字段列表和一个加密后的检索字段，遍历加密字段列表中的每个字段，检查字段是否包含加密后的检索字段，如果包含则将该字段添加到匹配结果列表中。

为了实现 Hash 算法的加密，我们使用了 Java 自带的 MessageDigest 类，并将其初始化为 SHA-256 算法。在对单个字符进行 Hash 加密时，我们首先将字符转换为字节数组，然后使用 update 方法更新 MessageDigest 对象的状态，最后使用 digest 方法生成字节数组的 Hash 值，并将其转换为十六进制字符串，最终返回字符串的前三位。


```java
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class SearchableEncryption {

    private static final String ENCRYPTION_ALGORITHM = "SHA-256";

    /**
     * 加密字段
     *
     * @param plaintext 待加密的字段明文
     * @return 密文扩展字段
     */
    public static String encryptField(String plaintext) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < plaintext.length(); i++) {
            String hash = hash(messageDigest, plaintext.charAt(i));
            stringBuilder.append(hash);
        }
        return stringBuilder.toString();
    }

    /**
     * 加密检索条件
     *
     * @param keyword 待检索的关键字
     * @return 加密后的检索字段
     */
    public static String encryptKeyword(String keyword) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < keyword.length(); i++) {
            String hash = hash(messageDigest, keyword.charAt(i));
            stringBuilder.append(hash);
        }
        return stringBuilder.toString();
    }

    /**
     * 执行模糊匹配
     *
     * @param database 数据库中存储的加密字段列表
     * @param keyword  加密后的检索字段
     * @return 匹配的结果列表
     */
    public static List<String> fuzzyMatch(List<String> database, String keyword) {
        List<String> results = new ArrayList<>();
        for (String field : database) {
            if (field.contains(keyword)) {
                results.add(field);
            }
        }
        return results;
    }

    /**
     * 对单个字符进行 Hash 加密并截取前三位
     *
     * @param messageDigest 加密算法
     * @param ch            待加密字符
     * @return 加密结果的前三位
     */
    private static String hash(MessageDigest messageDigest, char ch) throws Exception {
        messageDigest.update(String.valueOf(ch).getBytes());
        byte[] digest = messageDigest.digest();
        String hash = bytesToHex(digest);
        return hash.substring(0, 3);
    }

    /**
     * 将字节数组转换成十六进制字符串
     *
     * @param bytes 待转换字节数组
     * @return 转换后的十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }
}

```


参考资料：
[https://jos.jd.com/commondoc?listId=345](https://jos.jd.com/commondoc?listId=345)
缺点：
1. 有极小概率检索到多余的数据
2. Hash 算法需要带密钥防止破解


# Hash 算法介绍
Hash 函数又称单向散列函数（one-way hash function）是指对不同的输入值，通过单向散列函数进行计算，得到固定长度的输出值。这个输入值称为消息（message），输出值称为散列值（hash value）。
常用的 Hash 算法有很多种，每种算法都有自己的特点和优缺点。下面是几种常见的 Hash 算法对比：

## MD5（Message-Digest Algorithm 5）

MD5 是一种广泛使用的 Hash 算法，其输出长度为 128 位。虽然 MD5 有一定的安全性，但由于其输出长度较短，存在被暴力破解的风险。

## SHA-1（Secure Hash Algorithm 1）

SHA-1 是一种常用的 Hash 算法，其输出长度为 160 位。SHA-1 的安全性比 MD5 更高，但由于其输出长度较短，也存在被暴力破解的风险。

## SHA-2（Secure Hash Algorithm 2）

SHA-2 是一种比 SHA-1 更安全的 Hash 算法族，包括 SHA-224、SHA-256、SHA-384 和 SHA-512 四种算法。SHA-256 是其中应用最广泛的一种，其输出长度为 256 位。SHA-2 算法具有更高的安全性和更长的输出长度，但也需要更多的计算资源和时间。

## SHA-3（Secure Hash Algorithm 3）

SHA-3 是一种最新的 Hash 算法，由美国国家标准与技术研究院（NIST）于 2015 年发布。SHA-3 算法的安全性和效率都比 SHA-2 更高，但其应用较为局限，目前尚未得到广泛应用。

总的来说，选择哪种 Hash 算法取决于具体的应用场景和安全要求。一般来说，为了保证数据的安全性，应选择输出长度较长的 Hash 算法，如 SHA-2 算法族或 SHA-3 算法。同时，在实际应用中，还需要考虑算法的性能和适用性等方面的因素。



## 代码实现：
JDK的 java.security.MessageDigest 类为我们提供了消息摘要算法，用于  MD5 和 SHA 的散列值生成。下面代码做了简单的封装，便于直接使用。Java8 还不支持 SHA-3，从 Java9 开始支持 SHA-3

```java
public class MDUtil {

    /**
     * MD5 
     *
     * @param data 要哈希的数据
     * @return 32 位十六进制字符串
     */
    public static String MD5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data);
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * MD5 
     *
     * @param data 要哈希的数据
     * @return 32位十六进制字符串
     */
    public static String MD5(String data) {
        return MD5(data.getBytes());
    }

    /**
     * SHA-1 
     *
     * @param data 要哈希的数据
     * @return 40 位十六进制字符串
     */
    public static String SHA1(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(data);
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * SHA-1 
     *
     * @param data 要哈希的数据
     * @return 40 位十六进制字符串
     */
    public static String SHA1(String data) {
        return SHA1(data.getBytes());
    }

    /**
     * SHA-224 
     *
     * @param data 要哈希的数据
     * @return 56 位十六进制字符串
     */
    public static String SHA224(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-224");
            byte[] bytes = md.digest(data);
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * SHA-224 
     *
     * @param data 要哈希的数据
     * @return 56 位十六进制字符串
     */
    public static String SHA224(String data) {
        return SHA224(data.getBytes());
    }

    /**
     * SHA-256 
     *
     * @param data 要哈希的数据
     * @return 64 位十六进制字符串
     */
    public static String SHA256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(data);
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * SHA-256 
     *
     * @param data 要哈希的数据
     * @return 64 位十六进制字符串
     */
    public static String SHA256(String data) {
        return SHA256(data.getBytes());
    }

    /**
     * SHA-384 
     *
     * @param data 要哈希的数据
     * @return 96 位十六进制字符串
     */
    public static String SHA384(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            byte[] bytes = md.digest(data);
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * SHA-384 
     *
     * @param data 要哈希的数据
     * @return 96 位十六进制字符串
     */
    public static String SHA384(String data) {
        return SHA384(data.getBytes());
    }

    /**
     * SHA-512 
     *
     * @param data 要哈希的数据
     * @return 128 位十六进制字符串
     */
    public static String SHA512(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(data);
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * SHA-512 
     *
     * @param data 要哈希的数据
     * @return 128 位十六进制字符串
     */
    public static String SHA512(String data) {
        return SHA512(data.getBytes());
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hexValue = new StringBuilder();
        for (byte b : bytes) {
            int val = b & 0xFF;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

}
```
