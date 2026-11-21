package cl.rinconsur.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo no válido")
    @Size(max = 120, message = "El correo no puede pasar de 120 caracteres")
    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false, length = 100)
    private String contrasena;

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 12, message = "El RUT no puede pasar de 12 caracteres")
    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(length = 60)
    private String cargo;

    @Column(length = 9)
    private String telefono;

    @Column(length = 80)
    private String direccion;

    @Column(length = 60)
    private String comuna;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @NotNull(message = "El perfil es obligatorio")
    @Column(name = "perfil_id", nullable = false)
    private Long perfilId;

    @Column(length = 100)
    private String avatar;
}
