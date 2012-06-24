package net.aeten.core;

public class FieldTest<T> {
	enum Field {
		F1(1), F2("");

		final Object initialValue;

		Field(Object initial) {
			initialValue = initial;
		}
	}

	public static void main(String[] args) {
		EnumElement.Generic<Field>[] elements = EnumElement.elements(EnumElement.Generic.<Field> buildFactory(), Field.values());
		for (EnumElement.Generic<Field> element : elements) {
			System.err.println(element.ordinal() + " : " + element);
		}
		EnumElement<?>[] array = EnumElement.elements(EnumElement.Generic.<Object> buildFactory(), new Object[] { "toto", 0, 0f });
		for (EnumElement<?> element : array) {
			System.err.println(element.ordinal() + " : " + element);
		}
	}
}
