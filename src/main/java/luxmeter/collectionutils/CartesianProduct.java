package luxmeter.collectionutils;

import java.util.*;

final class CartesianProduct implements Iterable<List<Object>> {
    private final List<Collection<?>> vectors;
    private final int totalRowSize;
    private final int totalColumnSize;

    public CartesianProduct(Collection<?>... vectors) {
        this.vectors = Arrays.asList(vectors);
        totalRowSize = calcRowSize(this.vectors);
        totalColumnSize = this.vectors.size();
    }

    @Override
    public Iterator<List<Object>> iterator() {
        return new Iterator<List<Object>>() {
            private int row =0;

            @Override
            public boolean hasNext() {
                return row < totalRowSize;
            }

            @Override
            public List<Object> next() {
                return calcVectorAt(row++);
            }
        };
    }

    private List<Object> calcVectorAt(int rowIndex) {
        // i_0 = i / calcRowSize(vectors.subList(rowIndex, vectors.size()) % vectors.get(0).size()
        List<Object> currentCartesianProductVector = new ArrayList<>(vectors.size());
        for(int columnIndex = 0; columnIndex< totalColumnSize; columnIndex++) {
            List<Collection<?>> subVectors = vectors.subList(columnIndex, totalColumnSize);
            int currentTotalRowSize = calcRowSize(subVectors);
            List<?> currentVector = (List<?>) vectors.get(columnIndex);
            int currentVectorRowIndex = (rowIndex / (currentTotalRowSize / currentVector.size())) % currentVector.size();
            Object obj = currentVector.get(currentVectorRowIndex);
            currentCartesianProductVector.add(obj);
        }
        return currentCartesianProductVector;
    }

    private int calcRowSize(List<Collection<?>> lists) {
        return lists.stream().map(Collection::size).reduce((a, b) -> a * b).orElse(0);
    }
}