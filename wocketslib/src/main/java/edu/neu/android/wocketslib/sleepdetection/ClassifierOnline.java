package edu.neu.android.wocketslib.sleepdetection;


public class ClassifierOnline {
	
	public static double classify(String algorithm, int[] data) {
		double[] COEFFICIENT = null;
		if (algorithm.equalsIgnoreCase("sazonov")) {
			//Wocket Wrist 				constant 	 -8		-7		-6			-5		-4		-3			-2		-1		0		
			COEFFICIENT = new double[] {2.6493, -0.0009, -0.0001, -0.0005, -0.0003, -0.0006, -0.0005, -0.0011, -0.0013, -0.002};
			//Wocket Ankle
			//double[] COEFFICIENT = {3.1253, -0.001, -0.0001, -0.0006, -0.0004, -0.0009, -0.0008, -0.0017, -0.0024, -0.0026};
		} if (algorithm.equalsIgnoreCase("kripke")) {
			//Wocket Wrist
			COEFFICIENT = new double[] {2.8873, -0.0007, -0.0001, -0.0005, -0.0001, -0.0004, -0.0003,
					-0.0005, -0.0005, -0.0009, -0.0013, -0.0015, -0.0002, -0.0006};
			//Wocket Ankle
			/*double[] COEFFICIENT = {3.3773, -0.0008, -0.0002, -0.0004, -0.0002, -0.0004, -0.0005,
					-0.0006, -0.001, -0.0014, -0.0026, -0.0017, -0.0006, -0.0005};*/
		}
		double si = COEFFICIENT[0];
		for (int j = 1; j < COEFFICIENT.length; j++) 
			si += COEFFICIENT[j] * data[j-1];
		//double	psi = 1 / (1 + Math.exp(-si));
		//return psi;
		return si;
	}

	
	
}
