package de.netzfactor.servicedesk.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Der Schluessel, den alle drei Entities gemeinsam haben.
 *
 * <p>IDENTITY, weil die Spalten in der Datenbank <code>bigserial</code> sind -
 * die Nummer vergibt Postgres, nicht Hibernate.
 */
@MappedSuperclass
public abstract class Basis extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
}
