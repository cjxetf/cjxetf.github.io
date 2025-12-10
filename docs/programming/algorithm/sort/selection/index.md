# 选择排序（Selection sort）

## 1.流程

1. 从序列中找到最小的元素，与起始位置的元素交换位置
   
   执行完一轮后，起始位置的元素即为最小值

2. 忽略第 ① 步中找到的最小元素，重复执行步骤 ①，直到排序完成

> 同理，也可以找最大元素，则将其放到序列的末尾

<video src="./imgs/1.mp4" controls></video>

## 2.实现

以**找最大的元素，放到序列的末尾**为例

1. 以**整数**为例，先实现找出数组中最大的元素移动到最右边

    ```java
    public static void findAndMoveMax(int[] arr) {
        // maxIndex 为数组中最大元素的索引位置
        int maxIndex = 0;
        for (int begin = 1; begin < arr.length; begin++) {
            // 如果当前元素比已经记录的最大元素更大，则较换
            if (arr[begin] > arr[maxIndex]) {
                maxIndex = begin;
            }
        }
        // 最大元素与数组末尾元素进行交换位置
        int tmp = arr[arr.length - 1];
        arr[arr.length - 1] = arr[maxIndex];
        arr[maxIndex] = tmp;
    }
    ```

2. 完整实现
   
   `end`: 每轮比较应该把最大的元素放到哪个索引处
   
   `begin`: 从第 1 个元素开始，找到最大的元素并放到`end`位置

    ```java
    import sort.Sort;
    import utils.Integers;

    /**
    * 选择排序
    * 相较于冒泡排序，选择排序每轮先找出最大的元素，然后再交换位置，每轮只需一次交换
    *
    * @param <E> 元素类型，要求实现 Comparable 接口
    * @author yolk
    * @since 2024/6/20 11:20
    */
    public class SelectionSort<E extends Comparable<E>> extends Sort<E> {

        /**
        * 1.先实现找出数组中最大的元素移动到最右边
        */
        public static void findAndMoveMax(int[] arr) {
            // maxIndex 为数组中最大元素的索引位置
            int maxIndex = 0;
            for (int begin = 1; begin < arr.length; begin++) {
                // 如果当前元素比已经记录的最大元素更大，则较换
                if (arr[begin] > arr[maxIndex]) {
                    maxIndex = begin;
                }
            }
            // 最大元素与数组末尾元素进行交换位置
            int tmp = arr[arr.length - 1];
            arr[arr.length - 1] = arr[maxIndex];
            arr[maxIndex] = tmp;
        }

        /**
        * 2. 完整实现
        */
        @Override
        protected void sort() {
            for (int end = array.length - 1; end > 0; end--) {
                // maxIndex 为数组中最大元素的索引位置
                int maxIndex = 0;
                for (int begin = 1; begin <= end; begin++) {
                    // 如果当前元素比已经记录的最大元素更大，则记录索引
                    if (compare(begin, maxIndex) >= 0) {
                        maxIndex = begin;
                    }
                }
                // 最大元素与数组末尾元素进行交换位置
                swap(end, maxIndex);
            }
        }

        public static void main(String[] args) {
            // int[] arr = {5, 3, 8, 6, 2};
            // findAndMoveMax(arr);
            // System.out.println(Arrays.toString(arr));

            Integer[] arr = Integers.random(10, 1, 100);
            Integers.println(arr);

            Sort<Integer> sort = new SelectionSort<>();
            sort.sort(arr);
            Integers.println(arr);
        }

    }
    ```

    > 继承的`Sort`父类来自[封装公共父类](/programming/algorithm/sort/#_3-封装公共父类)
    > 
    > `Integers`类来自[工具类](/programming/algorithm/#_5-1-integers-java)

## 3.分析

选择排序和冒泡排序的思路类似，但是它实际上要比冒泡排序更高效一些，因为冒泡排序每次比较成功后都要交换位置，而选择排序只在每轮比较结束后，进行一次交换。

按照上述实现，选择排序的最坏、平均、最好时间复杂度均为`O(n^2)`，空间复杂度为`O(1)`，是**不稳定**、`In-place`的排序算法。

### 3.1.最好时间复杂度为什么是 O(n^2)？

仿照冒泡排序的[优化 ①](../bubble/#_3-1-优化-1-如果数组已经是有序的-可以提前终止排序) 方法对算法进行优化，如下：

```java
for (int end = arr.length - 1; end > 0; end--) {
    // 假设数组是有序的，则最大元素应该在 end 位置
    int maxIndex = end;
    boolean sorted = true; 
    for (int begin = 1; begin <= end; begin++) {
        if (compare(begin, maxIndex) >= 0) {
            // 发现新的最大值，说明数组不是有序的
            sorted = false; 
            maxIndex = begin;
        }
    }
    if (sorted) {
        // 提前终止排序
        break;
    }
    swap(end, maxIndex);
}
```

上述实现是正确的吗？答案是**否定**的。

假设有一个数组：`[4, 3, 2, 1, 5]`，第一次循环后,`sorted`变量依旧是`true`，因为最大值`5`在最后的位置上，符合有序数组的定义，因此提前终止了排序，但实际上数组并不是有序的。

选择排序只能判断某轮次中的最大值或最小值是否在正确的位置，无法判断数组是否有序。

### 3.2.为什么是不稳定算法？

假设有一个数组：`[4₁, 2, 3, 4₂, 1]`进行升序排序（这里的下标`₁`和`₂`只是为了区分两个值为 4 的元素）。按照选择排序的流程：

- 第 1 轮：在`[4₁, 2, 3, 4₂, 1]`中找到最大值`4₁`，与最后一个元素`1`交换，结果：`[1, 2, 3, 4₂, 4₁]`。
  
  注意： 现在`4₁`跑到了`4₂`的后面！

- 第 2 轮：在`[2, 1, 3, 4₂]`中找到最大值`4₂`，位置正确，不交换

- 第 3、4 轮：依次找到`3`和`2`，位置正确，不交换

最后数组完全有序，不过排序前：`4₁`在`4₂`前面，排序后：`4₂`在`4₁`前面，相对顺序被改变了！所以选择排序是不稳定的。

选择排序不稳定的根本原因在于**它进行的是长距离交换**，这个交换可能会**跳过**中间的其他相等元素，从而破坏稳定性。

既然是不稳定了，那就破罐子破摔，将前面实现中的比较`>=`改为`>`，这样还可以减少`maxIndex`被更新的次数。

```java
// `>=` 改为 `>`
if (compare(begin, maxIndex) > 0) {
    maxIndex = begin;
}
```
