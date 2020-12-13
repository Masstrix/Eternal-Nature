/*
 * Copyright 2020 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.util;

/**
 * Abstracts arithmetic and some math operations to work for any number type. This
 * is useful to use when you are not sure what the number type that will be used
 * but need to do some arithmetic operations.
 */
public class AbstractNumber {

    private Number number;

    public AbstractNumber(Number num) {
        this.number = num;
    }

    public AbstractNumber add(Number number) {
        this.number = AbstractNumber.add(this.number, number);
        return this;
    }

    public AbstractNumber subtract(Number number) {
        this.number = AbstractNumber.subtract(this.number, number);
        return this;
    }

    public AbstractNumber multiply(Number number) {
        this.number = AbstractNumber.multiply(this.number, number);
        return this;
    }

    public AbstractNumber divide(Number number) {
        this.number = AbstractNumber.divide(this.number, number);
        return this;
    }

    public AbstractNumber modulus(Number number) {
        this.number = AbstractNumber.modulus(this.number, number);
        return this;
    }

    public AbstractNumber increment() {
        this.number = AbstractNumber.increment(number);
        return this;
    }

    public AbstractNumber decrement() {
        this.number = AbstractNumber.decrement(number);
        return this;
    }

    public AbstractNumber abs() {
        this.number = AbstractNumber.abs(this.number);
        return this;
    }

    public boolean greaterThan(Number number) {
        return AbstractNumber.greaterThan(this.number, number);
    }

    public boolean equalTo(Number number) {
        return AbstractNumber.equal(this.number, number);
    }

    public boolean lessThan(Number number) {
        return AbstractNumber.lessThan(this.number, number);
    }

    public Number get() {
        return number;
    }

    //
    //  Static Methods
    //

    public static Number add(Number n1, Number n2) {
        return operate(ArithmeticType.ADD, n1, n2);
    }

    public static Number subtract(Number n1, Number n2) {
        return operate(ArithmeticType.SUBTRACT, n1, n2);
    }

    public static Number multiply(Number n1, Number n2) {
        return operate(ArithmeticType.MULTIPLY, n1, n2);
    }

    public static Number divide(Number n1, Number n2) {
        return operate(ArithmeticType.DIVIDE, n1, n2);
    }

    public static Number modulus(Number n1, Number n2) {
        return operate(ArithmeticType.MODULUS, n1, n2);
    }

    public static Number increment(Number num) {
        return operate(ArithmeticType.INCREMENT, num, num);
    }

    public static Number decrement(Number num) {
        return operate(ArithmeticType.DECREMENT, num, num);
    }

    public static Number abs(Number num) {
        return operate(ArithmeticType.ABS, num, num);
    }

    public static boolean greaterThan(Number n1, Number n2) {
        return compare(n1, n2) > 0;
    }

    public static boolean equal(Number n1, Number n2) {
        return compare(n1, n2) == 0;
    }

    public static boolean lessThan(Number n1, Number n2) {
        return compare(n1, n2) < 0;
    }

    public static int compare(Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            return Double.compare(n1.doubleValue(), n2.doubleValue());
        }
        if (n1 instanceof Float || n2 instanceof Float) {
            return Float.compare(n1.floatValue(), n2.floatValue());
        }
        if (n1 instanceof Long || n2 instanceof Long) {
            return Long.compare(n1.longValue(), n2.longValue());
        }
        if (n1 instanceof Integer || n2 instanceof Integer) {
            return Integer.compare(n1.intValue(), n2.intValue());
        }
        if (n1 instanceof Short || n2 instanceof Short) {
            return Short.compare(n1.shortValue(), n2.shortValue());
        }
        if (n1 instanceof Byte || n2 instanceof Byte) {
            return Byte.compare(n1.byteValue(), n2.byteValue());
        }
        return Double.compare(n1.doubleValue(), n2.doubleValue());
    }

    private enum ArithmeticType {
        ADD, SUBTRACT, DIVIDE, MULTIPLY, MODULUS, INCREMENT, DECREMENT, ABS
    }

    private static Number operate(ArithmeticType type, Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            switch (type) {
                case ADD:
                    return n1.doubleValue() + n2.doubleValue();
                case SUBTRACT:
                    return n1.doubleValue() - n2.doubleValue();
                case MULTIPLY:
                    return n1.doubleValue() * n2.doubleValue();
                case DIVIDE:
                    return n1.doubleValue() / n2.doubleValue();
                case MODULUS:
                    return 0;
                case INCREMENT: {
                    double d = n1.doubleValue();
                    d++;
                    return d;
                }
                case DECREMENT: {
                    double d = n1.doubleValue();
                    d--;
                    return d;
                }
                case ABS: {
                    return Math.abs(n1.doubleValue());
                }
                default:
                    return n1.doubleValue();
            }
        }
        if (n1 instanceof Float || n2 instanceof Float) {
            switch (type) {
                case ADD:
                    return n1.floatValue() + n2.floatValue();
                case SUBTRACT:
                    return n1.floatValue() - n2.floatValue();
                case MULTIPLY:
                    return n1.floatValue() * n2.floatValue();
                case DIVIDE:
                    return n1.floatValue() / n2.floatValue();
                case MODULUS:
                    return 0;
                case INCREMENT: {
                    double d = n1.floatValue();
                    d++;
                    return d;
                }
                case DECREMENT: {
                    double d = n1.floatValue();
                    d--;
                    return d;
                }
                case ABS: {
                    return Math.abs(n1.floatValue());
                }
                default:
                    return n1.floatValue();
            }
        }
        switch (type) {
            case ADD:
                return n1.longValue() + n2.longValue();
            case SUBTRACT:
                return n1.longValue() - n2.longValue();
            case MULTIPLY:
                return n1.longValue() * n2.longValue();
            case DIVIDE:
                return n1.longValue() / n2.longValue();
            case MODULUS:
                return 0;
            case INCREMENT: {
                double d = n1.longValue();
                d++;
                return d;
            }
            case DECREMENT: {
                double d = n1.longValue();
                d--;
                return d;
            }
            case ABS: {
                return Math.abs(n1.longValue());
            }
            default:
                return n1.longValue();
        }
    }
}
