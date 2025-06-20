package apitiendavideo.apitiendavideo.controladores;

import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import apitiendavideo.apitiendavideo.dtos.CambioClaveDTO;
import apitiendavideo.apitiendavideo.dtos.MensajeDTO;
import apitiendavideo.apitiendavideo.interfaces.IClienteServicio;
import apitiendavideo.apitiendavideo.interfaces.ITituloServicio;
import apitiendavideo.apitiendavideo.modelos.Cliente;
import apitiendavideo.apitiendavideo.repositorios.ClienteRepositorio;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

@RestController 
@RequestMapping("/clientes")
public class ClienteControlador {

    @Autowired
    private IClienteServicio servicio;

    @Autowired
    private ClienteRepositorio repositorio;

    @RequestMapping(value = "/listar", method = RequestMethod.GET)
    public List<Cliente> listar() {
        return servicio.listar();
    }

    @PatchMapping("/{id}/moroso")
    public ResponseEntity<Cliente> actualizarMoroso(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        Boolean nuevoMoroso = body.get("moroso");
        Cliente cliente = servicio.obtener(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        cliente.setMoroso(nuevoMoroso);
        Cliente actualizado = servicio.guardar(cliente);
        return ResponseEntity.ok(actualizado);
    }

    @RequestMapping(value = "/obtener/{id}", method = RequestMethod.GET)
    public Cliente obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @RequestMapping(value = "/buscar/{nombre}", method = RequestMethod.GET)
    public List<Cliente> buscar(@PathVariable String nombre) {
        return servicio.buscar(nombre);
    }
    
    @RequestMapping(value = "/agregar", method = RequestMethod.POST)
    public Cliente crear(@RequestBody Cliente cliente) {
        return servicio.guardar(cliente);
    }

    @RequestMapping(value = "/modificar", method = RequestMethod.PUT)
    public Cliente actualizar(@RequestBody Cliente cliente) {
        if (servicio.obtener(cliente.getId()) != null) {
            return servicio.guardar(cliente);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/eliminar/{id}", method = RequestMethod.DELETE)
    public boolean eliminar(@PathVariable String id) {
        return servicio.eliminar(id);
    }

    @GetMapping("/reporte")
    public void exportarClientesExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=clientes_reporte.xlsx");

        List<Cliente> clientes = servicio.listar();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte Clientes");

        // Encabezados
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Identificacion");
        header.createCell(1).setCellValue("Nombre");
        header.createCell(2).setCellValue("Apellido");
        header.createCell(3).setCellValue("Celular");
        header.createCell(4).setCellValue("Correo");
        header.createCell(5).setCellValue("Moroso");
        header.createCell(6).setCellValue("Activo");

        // Datos
        int rowNum = 1;
        for (Cliente c : clientes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getNombre());
            row.createCell(2).setCellValue(c.getApellido());
            row.createCell(3).setCellValue(c.getMovil());
            row.createCell(4).setCellValue(c.getCorreo());
            row.createCell(5).setCellValue(c.getMoroso());
            row.createCell(6).setCellValue(c.getActivo());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PutMapping("/cambiar-clave")
    public ResponseEntity<MensajeDTO> cambiarClave(@RequestBody CambioClaveDTO datos) {
        Optional<Cliente> clienteOptional = repositorio.findByCorreo(datos.getCorreo());

        if (!clienteOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new MensajeDTO("Usuario no encontrado"));
        }

        Cliente cliente = clienteOptional.get();

        if (!cliente.getClave().equals(datos.getClaveActual())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new MensajeDTO("Clave actual incorrecta"));
        }

        cliente.setClave(datos.getNuevaClave());
        repositorio.save(cliente);

        return ResponseEntity.ok(new MensajeDTO("Contraseña actualizada con éxito"));
    }
}
