import java.util.ArrayList;
import java.util.List;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/25 9:33
 */
public class LoopChain<E> {
    private static final class ChainNode<E> {
        E val;
        ChainNode<E> nextNode;

        /**
         * 构造方法
         *
         * @param val
         * @param nextNode
         */
        public ChainNode(E val, ChainNode<E> nextNode) {
            this.val = val;
            this.nextNode = nextNode;
        }
    }

    // files

    private final int chainLength;

    private ChainNode<E> head;

    private ChainNode<E> tail;

    private int size = 0;

    // constructor

    public LoopChain(int chainLength) {
        this.chainLength = chainLength;
    }


    // getter/setter

    /**
     * 获取环链长度
     */
    public int getChainLength() {
        return chainLength;
    }

    /**
     * 返回当前大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取头节点
     */
    public ChainNode getHead() {
        return head;
    }

    /**
     * 往环链中新增元素的方法
     *
     * @param val 元素值
     * @return >0 -> 还能新增几个;=0 -> 刚好形成环链;=-1 -> 环链已经形成正在替换
     */
    public int add(E val) {
        // 链中数量少于chainLength-1时直接往里面加
        if (size < chainLength - 1) {
            // 没有头节点表示第一个元素
            if (head == null) {
                head = new ChainNode<>(val, null);
                tail = head;
            } else {
                tail.nextNode = new ChainNode<>(val, null);
                tail = tail.nextNode;
            }
            return chainLength - ++size;
        } else if (size == chainLength - 1) {
            // 如果刚好等于chainLength-1时,需要在此时闭合链
            tail.nextNode = new ChainNode<>(val, head);
            tail = head;
            size++;
            return 0;
        } else {
            // 链表中数目已经足够时,从head开始重新覆盖
            head.val = val;
            // 头尾节点都往下顺延
            // 其实此时tail已经没太大作用了
            head = head.nextNode;
            tail = tail.nextNode;
            return -1;
        }
    }

    /**
     * 单向遍历,如果遍历到头返回null
     *
     * @param node
     * @return
     */
    public ChainNode<E> next(ChainNode<E> node) {
        if (node == tail || node.nextNode == head) {
            return null;
        }
        return node.nextNode;
    }

    /**
     * 以List形式保留原由顺序返回values
     *
     * @return List<E>
     */
    public List<E> valuesAsList() {
        List<E> res = new ArrayList<>(size);
        res.add(head.val);

        ChainNode<E> sign = head;
        while (null != (sign = next(sign))) {
            res.add(sign.val);
        }
        return res;
    }

    /**
     * 清空环链
     */
    public void clear(){
        head.val = null;
        head.nextNode =null;
        ChainNode t;
        while ((t = next(head)) != null){
            t.val = null;
            t.nextNode = null;
        }

        head = tail = null;
        size=0;
    }

}
