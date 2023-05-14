package src.utils;

public enum SPSymbs {

    TABLE_CORNER_TOP_LEFT('╔'),
    TABLE_CORNER_TOP_RIGHT('╗'),
    TABLE_CORNER_BOTTOM_LEFT('╚'),
    TABLE_CORNER_BOTTOM_RIGHT('╝'),
    TABLE_CONNECTION_UP('╩'),
    TABLE_CONNECTION_DOWN('╦'),
    TABLE_CONNECTION_RIGHT('╠'),
    TABLE_CONNECTION_LEFT('╣'),
    TABLE_CROSS_CONNECTION('╬'),
    TABLE_HORIZONTAL_LINE('═'),
    TABLE_VERTICAL_LINE('║');


    private Character symb;

    SPSymbs(Character symb) {
            this.symb = symb;
    }

    public Character getSymb() {
        return symb;
    }
}
