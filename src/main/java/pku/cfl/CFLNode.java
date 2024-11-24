package pku.cfl;

import pascal.taie.ir.exp.Var;

public enum CFLNode {
    VAR {
        private Var value;

        @Override
        public void setValue(Object value) {
            if (!(value instanceof Var)) {
                throw new IllegalArgumentException("Expected Var type for VAR tag");
            }
            this.value = (Var) value;
        }

        @Override
        public Var getVar() {
            return value;
        }

        @Override
        public Integer getAllocId() {
            throw new UnsupportedOperationException("VAR does not support getAllocId");
        }

        @Override
        public String toString() {
            return "VAR(" + value + ")";
        }
    }, ALLOC_ID {
        private Integer value;

        @Override
        public void setValue(Object value) {
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("Expected Integer type for ALLOC_ID tag");
            }
            this.value = (Integer) value;
        }

        @Override
        public Integer getAllocId() {
            return value;
        }

        @Override
        public Var getVar() {
            throw new UnsupportedOperationException("ALLOC_ID does not support getVar");
        }

        @Override
        public String toString() {
            return "ALLOC_ID(" + value + ")";
        }
    };

    public abstract void setValue(Object value);

    public abstract Var getVar();

    public abstract Integer getAllocId();

    // Factory methods
    public static CFLNode ofVar(Var var) {
        CFLNode node = VAR;
        node.setValue(var);
        return node;
    }

    public static CFLNode ofAllocId(Integer id) {
        CFLNode node = ALLOC_ID;
        node.setValue(id);
        return node;
    }
}
