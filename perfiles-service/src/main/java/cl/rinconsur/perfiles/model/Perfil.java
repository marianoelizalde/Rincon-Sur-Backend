package cl.rinconsur.perfiles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del perfil es obligatorio")
    @Size(max = 40, message = "El nombre no puede pasar de 40 caracteres")
    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    @Size(max = 120, message = "La descripcion no puede pasar de 120 caracteres")
    @Column(length = 120)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}
