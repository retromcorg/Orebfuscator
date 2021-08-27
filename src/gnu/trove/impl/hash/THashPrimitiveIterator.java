//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gnu.trove.impl.hash;

import gnu.trove.iterator.TPrimitiveIterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public abstract class THashPrimitiveIterator implements TPrimitiveIterator {
    protected final TPrimitiveHash _hash;
    protected int _expectedSize;
    protected int _index;

    public THashPrimitiveIterator(TPrimitiveHash hash) {
        this._hash = hash;
        this._expectedSize = this._hash.size();
        this._index = this._hash.capacity();
    }

    protected final int nextIndex() {
        if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
        } else {
            byte[] states = this._hash._states;
            int i = this._index;

            while(i-- > 0 && states[i] != 1) {
            }

            return i;
        }
    }

    public boolean hasNext() {
        return this.nextIndex() >= 0;
    }

    public void remove() {
        if (this._expectedSize != this._hash.size()) {
            throw new ConcurrentModificationException();
        } else {
            try {
                this._hash.tempDisableAutoCompaction();
                this._hash.removeAt(this._index);
            } finally {
                this._hash.reenableAutoCompaction(false);
            }

            this._hash.reenableAutoCompaction(false);
            --this._expectedSize;
        }
    }

    protected final void moveToNextIndex() {
        int nextIndex = this.nextIndex();
        this._index = nextIndex;
        if (nextIndex < 0) {
            throw new NoSuchElementException();
        }
    }
}
