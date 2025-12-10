# 堆排序（Heap Sort）

## 1.流程

1. 对序列进行原地建堆（[Heapify](/programming/data-structure/heap/#_5-5-堆化)）
   
2. 重复执行以下操作，直到堆中只剩下一个元素
   1. 将堆顶元素（当前堆中的最大值）与堆的最后一个元素交换位置
   2. 将堆的有效大小减一
   3. 对新的堆顶元素执行**下滤**操作，使其重新满足堆性质

**下滤**操作的时间复杂度为`O(logn)`，整个堆排序的时间复杂度为`O(nlogn)`。

![](./imgs/1.gif)

## 2.实现

1. 先要拷贝[堆化](/programming/data-structure/heap/#代码实现-2)的代码
   
    ```java
    private void heapify() {
        for (int i = (size >> 1) - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    private void siftDown(int index) {
        E e = this.elements[index];
        int half = size >> 1;
        while (index < half) {
            int childIndex = (index << 1) + 1;
            E child = this.elements[childIndex];
            int rightChildIndex = childIndex + 1;
            if (rightChildIndex < size && compare(this.elements[rightChildIndex], child) > 0) {
                child = this.elements[rightChildIndex];
                childIndex = rightChildIndex;
            }
            if (compare(e, child) >= 0) {
                break;
            }
            this.elements[index] = child;
            index = childIndex;
        }
        this.elements[index] = e;
    }
    ```

2. 完整实现

    ```java
    import sort.Sort;
    import utils.Integers;

    /**
    * 堆排序
    *
    * @param <E> 元素类型，要求实现 Comparable 接口
    * @author yolk
    * @since 2025/10/6 14:29
    */
    public class HeapSort<E extends Comparable<E>> extends Sort<E> {

        // 堆中元素数量
        private int heapSize;

        @Override
        protected void sort() {
            heapSize = array.length;

            // 堆化：原地建堆
            heapify();

            // 开始排序，如果堆中只剩下 1 个元素，则不需要排序了
            while (heapSize > 1) {
                /*
                将堆顶元素和堆中最后一个元素交换
                heapSize--，表示堆中少了一个元素
                */
                swap(0, --heapSize);

                // 交换后，堆顶元素可能不满足堆的性质，需要下滤
                siftDown(0);
            }
        }

        private void heapify() {
            for (int i = (heapSize >> 1) - 1; i >= 0; i--) {
                siftDown(i);
            }
        }

        private void siftDown(int index) {
            E e = array[index];
            int half = heapSize >> 1;
            while (index < half) {
                int childIndex = (index << 1) + 1;
                E child = array[childIndex];
                int rightChildIndex = childIndex + 1;
                if (rightChildIndex < heapSize && compare(array[rightChildIndex], child) > 0) {
                    child = array[rightChildIndex];
                    childIndex = rightChildIndex;
                }
                if (compare(e, child) >= 0) {
                    break;
                }
                array[index] = child;
                index = childIndex;
            }
            array[index] = e;
        }

        public static void main(String[] args) {
            Integer[] array = Integers.random(10, 1, 100);
            Integers.println(array);

            Sort<Integer> sort = new HeapSort<>();
            sort.sort(array);

            Integers.println(array);
        }

    }
    ```

   > 继承的`Sort`父类来自[封装公共父类](/programming/algorithm/sort/#_3-封装公共父类)
   > 
   > `Integers`类来自[工具类](/programming/algorithm/#_5-1-integers-java)

## 3.分析

- **堆排序**的最好、最坏、平均时间复杂度均为`O(nlogn)`，空间复杂度为`O(1)`，是一种`不稳定、In-place`的排序算法。

- **堆排序可以看作是对选择排序的一种优化**，因为选择排序每次都要遍历整个数组来找到最大值，而堆排序通过堆这种数据结构，可以更高效地找到最大值。

- 为什么是**不稳定**的排序算法？
   
   假如有一组数据：`5₁, 5₂, 2, 1, 4`，当进行堆排序时，需要将堆顶元素`5₁`与最后一个元素`4`交换位置，变为`4, 5₂, 2, 1, 5₁`，此时`5₁`和`5₂`的相对位置已经改变了。

