package com.ferme.frontend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatUtil {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(FormatUtil.class);


	/**
	 * Método para formatear rut Chileno.
	 * @param rut
	 * @return
	 */
	public static String formatRUT(String rut) {

		int cont = 0;
		String format;
		rut = rut.replace(".", "");
		rut = rut.replace("-", "");
		format = "-" + rut.substring(rut.length() - 1);
		for (int i = rut.length() - 2; i >= 0; i--) {
			format = rut.substring(i, i + 1) + format;
			cont++;
			if (cont == 3 && i != 0) {
				format = "." + format;
				cont = 0;
			}
		}
		return format;
	}
	
	/**
	 * Metodo para validar RUT Chileno.
	 * @param rut
	 * @return
	 */
	public static boolean validarRut(String rut) {
		boolean validacion = false;
		try {
			rut = rut.toUpperCase();
			rut = rut.replace(".", "");
			rut = rut.replace("-", "");
			int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));

			char dv = rut.charAt(rut.length() - 1);

			int m = 0, s = 1;
			for (; rutAux != 0; rutAux /= 10) {
				s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
			}
			if (dv == (char) (s != 0 ? s + 47 : 75)) {
				validacion = true;
			}

		} catch (java.lang.NumberFormatException e) {
			LOG.error("Error en el formato de rut. " + e.getMessage(), e);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return validacion;
	}

}
