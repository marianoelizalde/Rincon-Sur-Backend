package cl.rinconsur.registros.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_id", nullable = false)
    private Long sesionId;

    @Column(nullable = false, length = 5)
    private String hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Clasificacion clasificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "rango_etario", length = 20)
    private RangoEtario rangoEtario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compania", length = 20)
    private TipoCompania tipoCompania;

    @Column(name = "compra_otra_tienda", nullable = false)
    @Builder.Default
    private boolean compraOtraTienda = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean anulado = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;
}
