# 前言

## 1.工具类

### 1.1. BinaryTreePrinter.java

```java
import java.util.*;

/**
 * 让开发者能够直观地看到树的结构，比如二叉树、红黑树等。
 * <p>
 * 主要特点：
 * 1. 使用函数式接口，不需要类实现特定接口
 * 2. 支持自定义节点值的显示格式
 * 3. 支持高亮显示特殊节点（比如红黑树中的红色节点）
 * 4. 提供简单易用的静态方法和灵活的Builder模式
 *
 * @author yolk101336
 * @since 2025-10-05
 */
public class BinaryTreePrinter {

    // ========== 打印用的字符常量 ==========

    /**
     * 水平间距字符 - 用制表符来保证对齐
     */
    private static final String HORIZONTAL_SPACING = "\t";
    /**
     * 分支连接线 - 连接父子节点的横线
     */
    private static final String BRANCH_CONNECTOR = "____";
    /**
     * 垂直连接线 - 表示节点位置的竖线
     */
    private static final String VERTICAL_CONNECTOR = "|";
    /**
     * ANSI颜色代码 - 高亮开始（红色粗体）
     */
    private static final String HIGHLIGHT_START = "\033[91;1m";
    /**
     * ANSI颜色代码 - 高亮结束（重置颜色）
     */
    private static final String HIGHLIGHT_END = "\033[0m";

    // ========== 函数式接口定义 ==========

    /**
     * 根节点提供者接口
     * <p>
     * 这个接口用来获取树的根节点。
     * 实现者需要返回代表根节点的对象，如果树为空则返回null。
     */
    @FunctionalInterface
    public interface NodeProvider {
        /**
         * 获取树的根节点
         *
         * @return 根节点对象，如果树为空返回null
         */
        Object getRoot();
    }

    /**
     * 子节点提供者接口
     * <p>
     * 这个接口用来获取指定节点的左子节点或右子节点。
     * 实现者需要根据isLeft参数决定返回左子节点还是右子节点。
     */
    @FunctionalInterface
    public interface ChildProvider {
        /**
         * 获取指定节点的子节点
         *
         * @param node   父节点
         * @param isLeft true表示获取左子节点，false表示获取右子节点
         * @return 子节点对象，如果不存在返回null
         */
        Object getChild(Object node, boolean isLeft);
    }

    /**
     * 节点值提供者接口
     * <p>
     * 这个接口用来获取节点要显示的值。
     * 实现者可以自定义节点值的格式化方式。
     *
     * @param <T> 节点值的类型
     */
    @FunctionalInterface
    public interface ValueProvider<T> {
        /**
         * 获取节点的显示值
         *
         * @param node 要获取值的节点
         * @return 节点的显示值
         */
        T getValue(Object node);
    }

    /**
     * 节点高亮提供者接口
     * <p>
     * 这个接口用来判断某个节点是否需要高亮显示。
     * 比如在红黑树中，红色节点可能需要用不同颜色显示。
     */
    @FunctionalInterface
    public interface HighlightProvider {
        /**
         * 判断节点是否需要高亮显示
         *
         * @param node 要判断的节点
         * @return true表示需要高亮，false表示正常显示
         */
        boolean isHighlighted(Object node);
    }

    // ========== Builder模式配置类 ==========

    /**
     * 树打印配置构建器
     * <p>
     * 这个类使用Builder模式，让用户可以逐步配置树的打印参数。
     * 使用方式：
     * 1. 调用TreePrintBuilder.create()创建构建器
     * 2. 链式调用withXxx()方法设置各种参数
     * 3. 最后调用build()生成打印结果
     *
     * @param <T> 节点值的类型
     */
    public static class TreePrintBuilder<T> {
        /**
         * 根节点提供者 - 必须设置
         */
        private NodeProvider rootProvider;
        /**
         * 子节点提供者 - 必须设置
         */
        private ChildProvider childProvider;
        /**
         * 节点值提供者 - 必须设置
         */
        private ValueProvider<T> valueProvider;
        /**
         * 节点高亮提供者 - 可选，默认所有节点都不高亮
         */
        private HighlightProvider highlightProvider = node -> false;

        /**
         * 私有构造函数，防止直接new，必须通过create()方法创建
         */
        private TreePrintBuilder() {
        }

        /**
         * 静态工厂方法 - 创建一个新的构建器实例
         * <p>
         * 这是创建TreePrintBuilder的唯一方式，采用了工厂模式。
         *
         * @param <T> 节点值的类型
         * @return 新的构建器实例
         */
        public static <T> TreePrintBuilder<T> create() {
            return new TreePrintBuilder<>();
        }

        /**
         * 设置根节点提供者
         *
         * @param rootProvider 根节点提供者，不能为null
         * @return 当前构建器实例，支持链式调用
         */
        public TreePrintBuilder<T> withRoot(NodeProvider rootProvider) {
            this.rootProvider = rootProvider;
            return this;
        }

        /**
         * 设置子节点提供者
         *
         * @param childProvider 子节点提供者，不能为null
         * @return 当前构建器实例，支持链式调用
         */
        public TreePrintBuilder<T> withChildren(ChildProvider childProvider) {
            this.childProvider = childProvider;
            return this;
        }

        /**
         * 设置节点值提供者
         *
         * @param valueProvider 节点值提供者，不能为null
         * @return 当前构建器实例，支持链式调用
         */
        public TreePrintBuilder<T> withValues(ValueProvider<T> valueProvider) {
            this.valueProvider = valueProvider;
            return this;
        }

        /**
         * 设置节点高亮提供者（可选）
         *
         * @param highlightProvider 节点高亮提供者
         * @return 当前构建器实例，支持链式调用
         */
        public TreePrintBuilder<T> withHighlighting(HighlightProvider highlightProvider) {
            this.highlightProvider = highlightProvider;
            return this;
        }

        /**
         * 构建并生成树的字符串表示
         * <p>
         * 这个方法会检查必需的参数是否都已设置，然后创建树渲染器并生成结果。
         *
         * @return 树的ASCII艺术字符串表示
         * @throws NullPointerException 如果必需的参数没有设置
         */
        public String build() {
            // 检查必需参数 - 这三个参数是必须的，否则无法渲染树
            Objects.requireNonNull(rootProvider, "Root provider is required");
            Objects.requireNonNull(childProvider, "Child provider is required");
            Objects.requireNonNull(valueProvider, "Value provider is required");

            // 创建函数式树渲染器
            TreeRenderer<T> renderer = new FunctionalTreeRenderer<>(
                    rootProvider, childProvider, valueProvider, highlightProvider);
            // 创建树打印器并生成结果
            return new BinaryTreePrinter(renderer).generateTreeString();
        }
    }

    /**
     * 向后兼容的静态工厂方法
     * <p>
     * 这个方法是为了保持API的一致性，实际上调用的是TreePrintBuilder.create()。
     *
     * @param <T> 节点值类型
     * @return 新的构建器实例
     */
    public static <T> TreePrintBuilder<T> create() {
        return TreePrintBuilder.create();
    }

    // ========== 私有实现部分 ==========

    /**
     * 节点布局信息映射表 - 存储每个节点在水平方向上的位置信息
     */
    private final Map<Object, NodeLayoutInfo> nodeLayoutMap = new HashMap<>();
    /**
     * 树渲染器 - 提供树结构信息的接口
     */
    private final TreeRenderer<?> treeRenderer;

    /**
     * 私有构造函数
     * <p>
     * TreePrinter只能通过静态方法创建，这是为了保持API的简洁性。
     *
     * @param treeRenderer 树渲染器
     */
    private BinaryTreePrinter(TreeRenderer<?> treeRenderer) {
        this.treeRenderer = treeRenderer;
    }

    /**
     * 函数式树渲染器实现类
     * <p>
     * 这个类将用户提供的函数式接口适配到内部的TreeRenderer接口。
     * 它是一个适配器模式的实现。
     *
     * @param <T> 节点值类型
     */
    private static class FunctionalTreeRenderer<T> implements TreeRenderer<T> {
        /**
         * 根节点提供者
         */
        private final NodeProvider rootProvider;
        /**
         * 子节点提供者
         */
        private final ChildProvider childProvider;
        /**
         * 节点值提供者
         */
        private final ValueProvider<T> valueProvider;
        /**
         * 节点高亮提供者
         */
        private final HighlightProvider highlightProvider;

        /**
         * 构造函数
         *
         * @param rootProvider      根节点提供者
         * @param childProvider     子节点提供者
         * @param valueProvider     节点值提供者
         * @param highlightProvider 节点高亮提供者
         */
        FunctionalTreeRenderer(NodeProvider rootProvider, ChildProvider childProvider,
                               ValueProvider<T> valueProvider, HighlightProvider highlightProvider) {
            this.rootProvider = rootProvider;
            this.childProvider = childProvider;
            this.valueProvider = valueProvider;
            this.highlightProvider = highlightProvider;
        }

        @Override
        public Object getRoot() {
            return rootProvider.getRoot();
        }

        @Override
        public Object getLeftChild(Object node) {
            return childProvider.getChild(node, true);
        }

        @Override
        public Object getRightChild(Object node) {
            return childProvider.getChild(node, false);
        }

        @Override
        public T getNodeValue(Object node) {
            return valueProvider.getValue(node);
        }

        @Override
        public boolean isHighlighted(Object node) {
            return highlightProvider.isHighlighted(node);
        }
    }

    /**
     * 内部树渲染器接口
     * <p>
     * 这个接口定义了渲染树所需的基本操作。
     * 它是包私有的，外部用户无法直接使用，保证了封装性。
     *
     * @param <T> 节点值类型
     */
    interface TreeRenderer<T> {
        /**
         * 获取根节点
         */
        Object getRoot();

        /**
         * 获取左子节点
         */
        Object getLeftChild(Object node);

        /**
         * 获取右子节点
         */
        Object getRightChild(Object node);

        /**
         * 获取节点值
         */
        T getNodeValue(Object node);

        /**
         * 判断节点是否高亮
         */
        boolean isHighlighted(Object node);
    }

    /**
     * 节点布局信息类
     * <p>
     * 这个类存储节点在最终输出中的位置信息，
     * 用于计算节点之间的间距和连接线的位置。
     */
    private static class NodeLayoutInfo {
        /**
         * 节点在水平方向上的索引位置
         */
        final int horizontalIndex;
        /**
         * 节点值显示所需的宽度
         */
        final int displayWidth;

        /**
         * 构造函数
         *
         * @param horizontalIndex 水平索引
         * @param displayWidth    显示宽度
         */
        NodeLayoutInfo(int horizontalIndex, int displayWidth) {
            this.horizontalIndex = horizontalIndex;
            this.displayWidth = displayWidth;
        }
    }

    /**
     * 层级渲染数据类
     * <p>
     * 这个类用于存储渲染单个层级时的临时数据。
     * 每个层级需要渲染三行：上连接线、节点值行、下连接线。
     */
    private static class LevelRenderData {
        /**
         * 上层连接线 - 显示从父节点到当前节点的连接
         */
        StringBuilder topConnectorLine = new StringBuilder();
        /**
         * 节点值行 - 显示当前层级所有节点的值
         */
        StringBuilder nodeValueLine = new StringBuilder();
        /**
         * 下层连接线 - 显示从当前节点到子节点的连接
         */
        StringBuilder bottomConnectorLine = new StringBuilder();
        /**
         * 前一个处理的节点 - 用于计算间距
         */
        Object previousProcessedNode;
        /**
         * 最后一个右节点 - 用于连接线的计算
         */
        Object lastRightmostNode;

        /**
         * 将三行内容组合成字符串
         *
         * @return 当前层级的完整渲染结果
         */
        @Override
        public String toString() {
            return topConnectorLine + "\n" + nodeValueLine + "\n" + bottomConnectorLine + "\n";
        }
    }

    /**
     * 生成树的完整字符串表示
     * <p>
     * 这是整个打印过程的入口方法，它协调各个步骤来生成最终结果。
     *
     * @return 树的ASCII艺术字符串
     */
    public String generateTreeString() {
        Object root = treeRenderer.getRoot();
        if (root == null) {
            return "Empty Tree";
        }

        // 第一步：计算所有节点的布局位置
        calculateNodeLayout();
        // 第二步：按层级渲染树结构
        return renderLevelByLevel(Collections.singletonList(root));
    }

    /**
     * 计算节点布局
     * <p>
     * 这个方法通过中序遍历确定节点的水平位置。
     * 中序遍历可以保证左子节点在左边，右子节点在右边，
     * 这样渲染出的树结构看起来更自然。
     */
    private void calculateNodeLayout() {
        // 存储中序遍历的节点顺序
        List<Object> inOrderNodes = new ArrayList<>();
        // 执行中序遍历
        performInOrderTraversal(treeRenderer.getRoot(), inOrderNodes);

        // 累计宽度偏移 - 用于处理不同宽度的节点值
        int cumulativeWidthOffset = 0;
        for (int i = 0; i < inOrderNodes.size(); i++) {
            Object node = inOrderNodes.get(i);
            // 计算节点值的显示宽度（以4个字符为单位进行归一化）
            int nodeDisplayWidth = treeRenderer.getNodeValue(node).toString().length() / 4;
            // 存储节点的布局信息
            nodeLayoutMap.put(node, new NodeLayoutInfo(i + cumulativeWidthOffset, nodeDisplayWidth));
            // 更新累计偏移
            cumulativeWidthOffset += nodeDisplayWidth;
        }
    }

    /**
     * 执行中序遍历
     * <p>
     * 中序遍历的顺序是：左子树 -> 根节点 -> 右子树
     * 这样遍历出的节点顺序在水平方向上是从左到右的。
     *
     * @param node     当前节点
     * @param nodeList 存储遍历结果的列表
     */
    private void performInOrderTraversal(Object node, List<Object> nodeList) {
        if (node == null) return;

        // 递归遍历左子树
        performInOrderTraversal(treeRenderer.getLeftChild(node), nodeList);
        // 访问当前节点
        nodeList.add(node);
        // 递归遍历右子树
        performInOrderTraversal(treeRenderer.getRightChild(node), nodeList);
    }

    /**
     * 按层级渲染树结构
     * <p>
     * 这个方法使用递归的方式，逐层渲染树结构。
     * 每次处理一个层级的所有节点，然后递归处理下一层级。
     *
     * @param currentLevelNodes 当前层级的所有节点
     * @return 当前层级及其以下所有层级的渲染结果
     */
    private String renderLevelByLevel(List<Object> currentLevelNodes) {
        // 递归终止条件：当前层级没有节点
        if (currentLevelNodes.isEmpty()) return "";

        // 存储下一层级的节点
        List<Object> nextLevelNodes = new ArrayList<>();
        // 当前层级的渲染数据
        LevelRenderData levelData = new LevelRenderData();

        // 处理当前层级的每个节点
        for (Object node : currentLevelNodes) {
            processNodeForRendering(node, levelData, nextLevelNodes);
        }

        // 递归渲染下一层级，并将结果拼接
        return levelData.toString() + renderLevelByLevel(nextLevelNodes);
    }

    /**
     * 处理单个节点的渲染
     * <p>
     * 这个方法负责渲染单个节点，包括：
     * 1. 计算节点的位置
     * 2. 绘制连接线
     * 3. 显示节点值
     * 4. 收集子节点供下一层级处理
     *
     * @param node           要处理的节点
     * @param levelData      当前层级的渲染数据
     * @param nextLevelNodes 下一层级的节点列表
     */
    private void processNodeForRendering(Object node, LevelRenderData levelData,
                                         List<Object> nextLevelNodes) {
        // 计算与前一个节点的间距
        int spacingBeforeConnector = calculateSpacingBeforeConnector(node, levelData.previousProcessedNode);
        int spacingBeforeValue = calculateSpacingBeforeValue(node, levelData.previousProcessedNode);

        // 构建上层连接线（垂直线，表示节点位置）
        levelData.topConnectorLine.append(generateSpacing(spacingBeforeConnector)).append(VERTICAL_CONNECTOR);
        // 构建节点值行
        levelData.nodeValueLine.append(generateSpacing(spacingBeforeValue)).append(formatNodeValue(node));

        // 如果是叶子节点，不需要绘制下层连接线
        if (isLeafNode(node)) {
            levelData.previousProcessedNode = node;
            return;
        }

        // 构建下层连接线并收集子节点
        String childConnectors = buildChildConnectors(node, nextLevelNodes);
        Object leftmostChild = getLeftmostDescendant(node);
        int connectorSpacing = calculateConnectorSpacing(leftmostChild, levelData.lastRightmostNode);

        levelData.bottomConnectorLine.append(generateSpacing(connectorSpacing)).append(childConnectors);
        // 更新状态，为处理下一个节点做准备
        levelData.previousProcessedNode = node;
        levelData.lastRightmostNode = getRightmostDescendant(node);
    }

    /**
     * 构建子节点连接线
     * <p>
     * 这个方法负责绘制从父节点到子节点的连接线。
     * 连接线的格式是：左横线 + 垂直线 + 右横线
     *
     * @param parentNode 父节点
     * @param childNodes 子节点列表（用于收集子节点）
     * @return 连接线字符串
     */
    private String buildChildConnectors(Object parentNode, List<Object> childNodes) {
        Object leftChild = treeRenderer.getLeftChild(parentNode);
        Object rightChild = treeRenderer.getRightChild(parentNode);
        String leftConnector = "";
        String rightConnector = "";

        // 处理左子节点
        if (leftChild != null) {
            childNodes.add(leftChild);
            // 计算从左子节点到父节点的横线长度
            leftConnector = generateHorizontalLine(getHorizontalIndex(parentNode) - getHorizontalIndex(leftChild));
        }

        // 处理右子节点
        if (rightChild != null) {
            childNodes.add(rightChild);
            // 计算从父节点到右子节点的横线长度
            rightConnector = generateHorizontalLine(getHorizontalIndex(rightChild) - getHorizontalIndex(parentNode));
        }

        // 组合连接线：左横线 + 中心垂直线 + 右横线
        return leftConnector + VERTICAL_CONNECTOR + rightConnector;
    }

    // ========== 工具方法 ==========

    /**
     * 获取节点的水平索引位置
     *
     * @param node 节点
     * @return 水平索引，如果节点为null则返回0
     */
    private int getHorizontalIndex(Object node) {
        return node == null ? 0 : nodeLayoutMap.get(node).horizontalIndex;
    }

    /**
     * 获取节点的显示宽度
     *
     * @param node 节点
     * @return 显示宽度，如果节点为null则返回0
     */
    private int getDisplayWidth(Object node) {
        return node == null ? 0 : nodeLayoutMap.get(node).displayWidth;
    }

    /**
     * 判断是否为叶子节点
     *
     * @param node 节点
     * @return true表示是叶子节点（没有子节点）
     */
    private boolean isLeafNode(Object node) {
        return treeRenderer.getLeftChild(node) == null && treeRenderer.getRightChild(node) == null;
    }

    /**
     * 获取最左边的后代节点
     * <p>
     * 如果有左子节点就返回左子节点，否则返回自己。
     * 这用于确定连接线的起始位置。
     *
     * @param node 节点
     * @return 最左边的后代节点
     */
    private Object getLeftmostDescendant(Object node) {
        return treeRenderer.getLeftChild(node) != null ? treeRenderer.getLeftChild(node) : node;
    }

    /**
     * 获取最右边的后代节点
     * <p>
     * 如果有右子节点就返回右子节点，否则返回自己。
     * 这用于确定连接线的结束位置。
     *
     * @param node 节点
     * @return 最右边的后代节点
     */
    private Object getRightmostDescendant(Object node) {
        return treeRenderer.getRightChild(node) != null ? treeRenderer.getRightChild(node) : node;
    }

    /**
     * 计算连接符之前的间距
     *
     * @param currentNode  当前节点
     * @param previousNode 前一个节点
     * @return 需要的间距数量
     */
    private int calculateSpacingBeforeConnector(Object currentNode, Object previousNode) {
        return getHorizontalIndex(currentNode) - getHorizontalIndex(previousNode);
    }

    /**
     * 计算节点值之前的间距
     * <p>
     * 这个计算需要考虑前一个节点的显示宽度。
     *
     * @param currentNode  当前节点
     * @param previousNode 前一个节点
     * @return 需要的间距数量
     */
    private int calculateSpacingBeforeValue(Object currentNode, Object previousNode) {
        return getHorizontalIndex(currentNode) - getHorizontalIndex(previousNode) - getDisplayWidth(previousNode);
    }

    /**
     * 计算连接器的间距
     *
     * @param leftmostChild 最左边的子节点
     * @param lastRightNode 最后一个右节点
     * @return 需要的间距数量
     */
    private int calculateConnectorSpacing(Object leftmostChild, Object lastRightNode) {
        return getHorizontalIndex(leftmostChild) - getHorizontalIndex(lastRightNode);
    }

    /**
     * 生成指定数量的空格字符串
     *
     * @param count 空格数量
     * @return 空格字符串，如果count <= 0则返回空字符串
     */
    private String generateSpacing(int count) {
        return count <= 0 ? "" : HORIZONTAL_SPACING.repeat(count);
    }

    /**
     * 生成指定长度的水平连接线
     *
     * @param length 连接线长度
     * @return 连接线字符串，如果length <= 0则返回空字符串
     */
    private String generateHorizontalLine(int length) {
        return length <= 0 ? "" : BRANCH_CONNECTOR.repeat(length);
    }

    /**
     * 格式化节点值
     * <p>
     * 如果节点需要高亮显示，会添加ANSI颜色代码。
     *
     * @param node 要格式化的节点
     * @return 格式化后的节点值字符串
     */
    private String formatNodeValue(Object node) {
        String value = treeRenderer.getNodeValue(node).toString();
        // 如果节点需要高亮，添加颜色代码
        return treeRenderer.isHighlighted(node) ? HIGHLIGHT_START + value + HIGHLIGHT_END : value;
    }
}
```

#### 使用示例

1. 可以通过重写 toString 方法，集成到你的数据结构中
   
    ```java
    // 假设我们有一个二叉树类
    class BinaryTree<T> {
        @Override
        public String toString() {
            if (size == 0) {
                return "Empty Heap";
            }
            return BinaryTreePrinter.TreePrintBuilder.<String>create()
                    .withRoot(() -> {
                        // TODO 返回根节点
                    })
                    .withChildren((node, isLeft) -> {
                        if (node == null) return null;
                        // TODO 根据 isLeft 返回左右子节点
                    })
                    .withValues(node -> {
                        if (node == null) return null;
                        // TODO 返回节点的值
                    })
                    .withHighlighting(node -> {
                        if (node == null) return false;
                        // TODO 根据节点属性决定是否高亮
                    })
                    .build();
        }
    }   
    ```

2. 直接使用 TreePrintBuilder 来打印
   
    ```java
    public static void main(String[] args) {
        // 假设我们有一个二叉树
        BinaryTree<Integer> tree = new BinaryTree<>();

        // 添加一些节点
        ....

        String result = TreePrinter.TreePrintBuilder.<String>create()
                .withRoot(() -> tree.size() == 0 ? null : 0)
                .withChildren((node, isLeft) -> {
                    if (node == null) return null;

                    // 根据实际节点类型调整
                    return isLeft ? node.left : node.right; 
                })
                .withValues(node -> {
                    if (node == null) return null;
                    return node.value.toString();
                })
                .withHighlighting(node -> {
                    if (node == null) return false;

                    // 例如红黑树中红色节点高亮
                    return node.color == Color.RED; 
                })
                .build();
        System.out.println(result);
    }
    ```


