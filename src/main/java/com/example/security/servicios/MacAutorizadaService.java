package com.example.security.servicios;

import com.example.security.modelo.Dispositivo;
import com.example.security.modelo.EstadoAutorizacion;
import com.example.security.modelo.MacAutorizada;
import com.example.security.repositorio.DispositivoRepository;
import com.example.security.repositorio.MacAutorizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MacAutorizadaService {

    @Autowired
    private MacAutorizadaRepository macAutorizadaRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    public List<MacAutorizada> findAll() {
        return macAutorizadaRepository.findAll();
    }

    /**
     * Solo las MACs ACTIVAS autorizan dispositivos. Una MAC desactivada deja de
     * autorizar sin perder su registro.
     */
    public boolean esMacAutorizada(String mac) {
        if (mac == null) return false;
        MacAutorizada registro = macAutorizadaRepository.findByMac(MacAutorizada.normalizar(mac));
        return registro != null && registro.isActivo();
    }

    /**
     * Existe un registro para esa MAC (activo o inactivo). Lo usa el escáner
     * ARP para distinguir una MAC conocida pero revocada (NO_AUTORIZADO) de una
     * MAC desconocida (DESCONOCIDO).
     */
    public boolean existeMac(String mac) {
        if (mac == null) return false;
        return macAutorizadaRepository.findByMac(MacAutorizada.normalizar(mac)) != null;
    }

    /**
     * Agrega una MAC autorizada sin duplicar registros:
     * <ol>
     *   <li>Si no existe, crea un registro activo.</li>
     *   <li>Si existe y está activo, devuelve el existente (sin duplicar).</li>
     *   <li>Si existe y está inactivo, reactiva el mismo registro conservando
     *       su identificador y fecha de registro.</li>
     * </ol>
     * En todos los casos sincroniza los dispositivos asociados como AUTORIZADOS.
     */
    @Transactional
    public MacAutorizada agregarMac(String mac, String descripcion) {
        String macNormalizada = MacAutorizada.normalizar(mac);
        MacAutorizada existente = macAutorizadaRepository.findByMac(macNormalizada);

        MacAutorizada resultado;
        if (existente == null) {
            resultado = macAutorizadaRepository.save(new MacAutorizada(macNormalizada, descripcion));
        } else if (!existente.isActivo()) {
            // Reactivar el MISMO registro: se conservan id y fechaRegistro
            existente.setActivo(true);
            resultado = macAutorizadaRepository.save(existente);
        } else {
            resultado = existente;
        }

        actualizarEstadoDispositivos(macNormalizada, EstadoAutorizacion.AUTORIZADO);
        return resultado;
    }

    /**
     * Desactiva una MAC autorizada conservando el registro, su fecha de
     * registro y su historial. Los dispositivos asociados pasan a
     * NO_AUTORIZADO; no se elimina ningún dispositivo.
     *
     * @throws NoSuchElementException    si la MAC no existe
     * @throws IllegalStateException si ya está desactivada (transición inválida)
     */
    @Transactional
    public MacAutorizada desactivarMac(Long id) {
        MacAutorizada mac = buscarPorId(id);
        if (!mac.isActivo()) {
            throw new IllegalStateException("La MAC ya está desactivada (ID: " + id + ")");
        }
        mac.setActivo(false);
        MacAutorizada guardada = macAutorizadaRepository.save(mac);
        actualizarEstadoDispositivos(guardada.getMac(), EstadoAutorizacion.NO_AUTORIZADO);
        return guardada;
    }

    /**
     * Reactiva una MAC desactivada conservando su identificador y fecha de
     * registro. Los dispositivos asociados vuelven a AUTORIZADO.
     *
     * @throws NoSuchElementException    si la MAC no existe
     * @throws IllegalStateException si ya está activa (transición inválida)
     */
    @Transactional
    public MacAutorizada reactivarMac(Long id) {
        MacAutorizada mac = buscarPorId(id);
        if (mac.isActivo()) {
            throw new IllegalStateException("La MAC ya está activa (ID: " + id + ")");
        }
        mac.setActivo(true);
        MacAutorizada guardada = macAutorizadaRepository.save(mac);
        actualizarEstadoDispositivos(guardada.getMac(), EstadoAutorizacion.AUTORIZADO);
        return guardada;
    }

    private MacAutorizada buscarPorId(Long id) {
        return macAutorizadaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("MAC no encontrada con ID: " + id));
    }

    /**
     * Sincroniza el estado de autorización de los dispositivos cuya MAC
     * coincida (normalizada). No modifica dispositivos de otras MACs y nunca
     * borra registros. Va en la misma transacción que el cambio de estado de la
     * MAC para no dejar la actualización parcialmente aplicada.
     */
    private void actualizarEstadoDispositivos(String mac, EstadoAutorizacion estado) {
        for (Dispositivo dispositivo : dispositivoRepository.findByMac(mac)) {
            dispositivo.setEstadoAutorizacion(estado);
            dispositivoRepository.save(dispositivo);
        }
    }
}
