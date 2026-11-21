package cl.rinconsur.usuarios.util;

import cl.rinconsur.usuarios.exception.ReglaNegocioException;

public final class Validaciones {

    private Validaciones() {
    }

    public static String nombre(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.length() < 3) throw new ReglaNegocioException("El nombre debe tener minimo 3 caracteres");
        if (limpio.length() > 80) throw new ReglaNegocioException("El nombre no puede pasar de 80 caracteres");
        for (char c : limpio.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                throw new ReglaNegocioException("El nombre solo puede tener letras y espacios");
            }
        }
        return limpio;
    }

    public static String correo(String valor) {
        String limpio = valor == null ? "" : valor.trim().toLowerCase();
        if (!limpio.contains("@") || !limpio.contains(".") || limpio.length() > 120) {
            throw new ReglaNegocioException("Correo no valido (ej: nombre@correo.cl)");
        }
        return limpio;
    }

    public static String cargo(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.length() < 3) throw new ReglaNegocioException("El cargo debe tener minimo 3 caracteres");
        if (limpio.length() > 60) throw new ReglaNegocioException("El cargo no puede pasar de 60 caracteres");
        return limpio;
    }

    public static String telefono(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) return "";
        if (limpio.length() != 9 || !limpio.chars().allMatch(Character::isDigit)) {
            throw new ReglaNegocioException("El telefono debe tener 9 digitos");
        }
        return limpio;
    }

    public static String direccion(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (!limpio.isEmpty() && limpio.length() < 5) throw new ReglaNegocioException("La direccion debe tener minimo 5 caracteres");
        if (limpio.length() > 80) throw new ReglaNegocioException("La direccion no puede pasar de 80 caracteres");
        return limpio;
    }

    public static String comuna(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) return "";
        if (limpio.length() < 3) throw new ReglaNegocioException("La comuna debe tener minimo 3 caracteres");
        for (char c : limpio.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                throw new ReglaNegocioException("La comuna solo puede tener letras y espacios");
            }
        }
        return limpio;
    }

    public static void contrasena(String valor) {
        if (valor == null || valor.isBlank()) throw new ReglaNegocioException("Ingresa una contrasena");
        if (valor.length() < 8) throw new ReglaNegocioException("La contrasena debe tener minimo 8 caracteres");
        if (valor.chars().noneMatch(Character::isUpperCase)) throw new ReglaNegocioException("La contrasena debe tener una mayuscula");
        if (valor.chars().noneMatch(Character::isLowerCase)) throw new ReglaNegocioException("La contrasena debe tener una minuscula");
        if (valor.chars().noneMatch(Character::isDigit)) throw new ReglaNegocioException("La contrasena debe tener un numero");
        if (valor.chars().allMatch(Character::isLetterOrDigit)) throw new ReglaNegocioException("La contrasena debe tener un caracter especial (ej: ! # $ %)");
    }

    public static String rut(String texto) {
        StringBuilder sb = new StringBuilder();
        if (texto != null) {
            for (char c : texto.toCharArray()) {
                if (Character.isLetterOrDigit(c)) sb.append(Character.toUpperCase(c));
            }
        }
        String limpio = sb.toString();
        if (limpio.length() < 2) throw new ReglaNegocioException("RUT no valido (ej: 12345678-5)");
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        String dv = limpio.substring(limpio.length() - 1);
        if (cuerpo.length() < 7 || cuerpo.length() > 8 || !cuerpo.chars().allMatch(Character::isDigit)
                || !dv.equals(digitoVerificador(cuerpo))) {
            throw new ReglaNegocioException("RUT no valido (ej: 12345678-5)");
        }
        return cuerpo + "-" + dv;
    }

    private static String digitoVerificador(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - suma % 11;
        if (resto == 11) return "0";
        if (resto == 10) return "K";
        return String.valueOf(resto);
    }
}
