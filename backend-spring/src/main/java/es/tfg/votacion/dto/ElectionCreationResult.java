package es.tfg.votacion.dto;

import es.tfg.votacion.model.Election;

/**
 * Resultado de la creación de una elección.
 * Incluye la clave privada que DEBE ser guardada por el administrador
 * ya que no se almacena en el servidor.
 */
public record ElectionCreationResult(
    Election election,
    String privateKey
) {}
