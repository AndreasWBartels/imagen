package org.locationtech.rpe;

public class Affine extends OperationBuilder {
	public static final String NAME = "affine";
	private static final String SY = "sy";
	private static final String SX = "sx";
	private static final String TY = "ty";
	private static final String TX = "tx";
	private static final String SHY = "shy";
	private static final String SHX = "shx";
	private static final String VECY = "vecy";
	private static final String VECX = "vecx";

	public Affine() {
		super(NAME);
	}

	Affine scale(double sx, double sy) {
		parameter(SX, sx);
		parameter(SY, sy);
		return this;
	}

	Affine rotate(double vecx, double vecy) {
		parameter(VECX, vecx);
		parameter(VECY, vecy);
		return this;
	}

	Affine shear(double shx, double shy) {
		parameter(SHX, shx);
		parameter(SHY, shy);
		return this;
	}

	Affine translate(double tx, double ty) {
		parameter(TX, tx);
		parameter(TY, ty);
		return this;
	}
}