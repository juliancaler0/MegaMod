package com.ultra.megamod.feature.baritone.pathfinding;

/**
 * Min-heap priority queue for A* open set.
 * Adapted from Baritone's optimized binary heap.
 */
public class BinaryHeapOpenSet {
    private PathNode[] heap;
    private int size;

    public BinaryHeapOpenSet() {
        this(1024);
    }

    public BinaryHeapOpenSet(int initialCapacity) {
        this.heap = new PathNode[initialCapacity];
        this.size = 0;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void insert(PathNode node) {
        if (size >= heap.length - 1) {
            grow();
        }
        size++;
        heap[size] = node;
        node.heapIndex = size;
        node.isOpen = true;
        siftUp(size);
    }

    public PathNode removeLowest() {
        if (size == 0) return null;
        PathNode min = heap[1];
        heap[1] = heap[size];
        heap[1].heapIndex = 1;
        heap[size] = null;
        size--;
        if (size > 0) {
            siftDown(1);
        }
        min.isOpen = false;
        min.heapIndex = -1;
        return min;
    }

    public void update(PathNode node) {
        siftUp(node.heapIndex);
    }

    private void siftUp(int index) {
        PathNode node = heap[index];
        while (index > 1) {
            int parent = index >> 1;
            if (heap[parent].combinedCost <= node.combinedCost) break;
            heap[index] = heap[parent];
            heap[index].heapIndex = index;
            index = parent;
        }
        heap[index] = node;
        node.heapIndex = index;
    }

    private void siftDown(int index) {
        PathNode node = heap[index];
        int half = size >> 1;
        while (index <= half) {
            int child = index << 1;
            int right = child + 1;
            if (right <= size && heap[right].combinedCost < heap[child].combinedCost) {
                child = right;
            }
            if (node.combinedCost <= heap[child].combinedCost) break;
            heap[index] = heap[child];
            heap[index].heapIndex = index;
            index = child;
        }
        heap[index] = node;
        node.heapIndex = index;
    }

    private void grow() {
        PathNode[] newHeap = new PathNode[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;
    }
}
