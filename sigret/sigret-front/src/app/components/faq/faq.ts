import { Component, signal, computed, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

interface FAQ {
  question: string;
  answer: string;
  diagram?: string;
}

interface Module {
  id: string;
  name: string;
  icon: string;
  faqs: FAQ[];
  expanded?: boolean;
}

@Component({
  selector: 'app-faq',
  imports: [CommonModule],
  templateUrl: './faq.html',
  styleUrl: './faq.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FaqComponent {
  private sanitizer = inject(DomSanitizer);

  selectedModuleId = signal<string>('servicios');

  modules = signal<Module[]>([
    {
      id: 'servicios',
      name: 'Servicios',
      icon: 'pi pi-wrench',
      expanded: false,
      faqs: [
        {
          question: '¿Cuál es el flujo completo de un servicio estándar?',
          answer: 'Un servicio comienza cuando se recibe un equipo en el taller (estado RECIBIDO). Luego se elabora un presupuesto, pasando a PRESUPUESTADO. Si el cliente aprueba, pasa a APROBADO. Al iniciar la orden de trabajo cambia a EN REPARACIÓN. Cuando el técnico finaliza, queda TERMINADO. Finalmente, al entregar el equipo con la firma de conformidad del cliente, se marca como FINALIZADO. Si el cliente rechaza el presupuesto, el servicio queda en estado RECHAZADO.',
          diagram: `
            <div class="flow-diagram">
              <div class="flow-title">Flujo de un Servicio Estándar</div>
              <div class="flow-row">
                <div class="flow-node node-info">RECIBIDO</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-warn">PRESUPUESTADO</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-info">APROBADO</div>
              </div>
              <div class="flow-connector-down"></div>
              <div class="flow-row">
                <div class="flow-node node-info">EN REPARACIÓN</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-success">TERMINADO</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-success">FINALIZADO</div>
              </div>
              <div class="flow-branch">
                <div class="flow-node node-danger">RECHAZADO</div>
                <div class="flow-branch-label">Si el cliente rechaza el presupuesto</div>
              </div>
            </div>`
        },
        {
          question: '¿Cómo creo un nuevo servicio?',
          answer: 'Desde la sección "Servicios", hacé clic en "Nuevo Servicio". Seleccioná o creá el cliente y su equipo. Describí el problema reportado por el cliente y agregá cualquier observación inicial. Debés capturar la firma de ingreso del cliente en la pantalla. El servicio se creará automáticamente en estado RECIBIDO y se le asignará un número de servicio único.'
        },
        {
          question: '¿Qué tipos de ingreso existen?',
          answer: 'Existen dos tipos de ingreso: "Cliente Trae Equipo" (cuando el cliente lleva el equipo al taller) y "Empresa Busca Equipo" (cuando el taller va a buscar el equipo al domicilio del cliente). El tipo de ingreso se selecciona al crear el servicio.'
        },
        {
          question: '¿Cómo funciona el tablero Kanban de servicios?',
          answer: 'El tablero Kanban muestra los servicios organizados en columnas por estado: Presupuestado, Aprobado, En Reparación, Terminado y Rechazado. Los servicios en estado RECIBIDO aparecen en una sección especial en la parte superior. Podés arrastrar y soltar servicios entre columnas para cambiar su estado rápidamente. Los servicios de garantía no aparecen en este tablero.'
        },
        {
          question: '¿Cómo busco un servicio específico?',
          answer: 'Usá la sección "Servicios - Buscar". Podés filtrar por número de servicio, estado, empleado asignado, tipo de equipo, rango de fechas de creación, y si es o no un servicio de garantía. Los resultados muestran el estado con un indicador de color para identificarlos rápidamente.'
        },
        {
          question: '¿Cómo finalizo un servicio y entrego el equipo?',
          answer: 'Cuando el servicio está en estado TERMINADO, desde el detalle del servicio podés hacer clic en "Finalizar Servicio". Es obligatorio capturar la firma de conformidad del cliente antes de completar la entrega. Al finalizar, el servicio pasa a estado FINALIZADO y se registra la fecha de devolución real del equipo.'
        },
        {
          question: '¿Puedo modificar un servicio una vez creado?',
          answer: 'Sí, podés editar la información del servicio y cambiar su estado según avance el proceso. Sin embargo, algunos cambios de estado son automáticos y están controlados por las acciones sobre presupuestos y órdenes de trabajo. Por ejemplo, al aprobar un presupuesto el servicio pasa automáticamente a APROBADO.'
        }
      ]
    },
    {
      id: 'presupuestos',
      name: 'Presupuestos',
      icon: 'pi pi-file-edit',
      expanded: false,
      faqs: [
        {
          question: '¿Cuál es el ciclo de vida de un presupuesto?',
          answer: 'Un presupuesto comienza en estado PENDIENTE (se crea automáticamente junto con el servicio). Un empleado lo toma y pasa a EN CURSO mientras lo elabora. Al completar los ítems, se marca como LISTO. Desde LISTO se puede enviar por email al cliente, pasando a ENVIADO. El cliente puede aprobarlo (APROBADO) o rechazarlo (RECHAZADO). Si pasa la fecha de vencimiento sin respuesta, se marca automáticamente como VENCIDO, pero se puede reenviar.',
          diagram: `
            <div class="flow-diagram">
              <div class="flow-title">Ciclo de Vida del Presupuesto</div>
              <div class="flow-row">
                <div class="flow-node node-warn">PENDIENTE</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-info">EN CURSO</div>
                <div class="flow-arrow flow-arrow-bidirectional"></div>
                <div class="flow-node node-secondary">LISTO</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-contrast">ENVIADO</div>
              </div>
              <div class="flow-outcomes">
                <div class="flow-outcome">
                  <div class="flow-outcome-arrow outcome-success"></div>
                  <div class="flow-node node-success">APROBADO</div>
                </div>
                <div class="flow-outcome">
                  <div class="flow-outcome-arrow outcome-danger"></div>
                  <div class="flow-node node-danger">RECHAZADO</div>
                </div>
                <div class="flow-outcome">
                  <div class="flow-outcome-arrow outcome-warn"></div>
                  <div class="flow-node node-warn">VENCIDO</div>
                  <div class="flow-outcome-note">Se puede reenviar</div>
                </div>
              </div>
            </div>`
        },
        {
          question: '¿Cómo elaboro un presupuesto?',
          answer: 'Desde el tablero de presupuestos, tomá un presupuesto PENDIENTE (pasará a EN CURSO y se te asignará automáticamente). Agregá los ítems de trabajo con sus descripciones, cantidades y precios. Podés incluir precios originales y alternativos para darle opciones al cliente. Cuando termines, marcalo como LISTO para que quede disponible para enviar.'
        },
        {
          question: '¿Cómo envío el presupuesto al cliente?',
          answer: 'Cuando el presupuesto está en estado LISTO, hacé clic en "Enviar". El sistema enviará un email al cliente con un enlace público y seguro donde podrá ver el detalle del presupuesto y aprobarlo o rechazarlo. El enlace tiene fecha de vencimiento configurable. El presupuesto pasará a estado ENVIADO.'
        },
        {
          question: '¿Qué pasa si el presupuesto vence?',
          answer: 'Si la fecha de vencimiento del presupuesto pasa sin que el cliente responda, el sistema lo marca automáticamente como VENCIDO. Desde el detalle podés actualizar los precios, cambiar la fecha de vencimiento y reenviar el presupuesto al cliente. El presupuesto volverá a estado ENVIADO con un nuevo enlace.'
        },
        {
          question: '¿Puedo volver a editar un presupuesto marcado como LISTO?',
          answer: 'Sí, desde el detalle del presupuesto en estado LISTO podés usar la opción "Volver a Editar" que lo regresará a estado EN CURSO, permitiéndote modificar los ítems nuevamente.'
        },
        {
          question: '¿Qué son los precios originales y alternativos?',
          answer: 'Cada ítem del presupuesto puede tener un precio original y un precio alternativo. Esto permite ofrecer al cliente opciones (por ejemplo, repuesto original vs. genérico). Al aprobar, el cliente indica qué opción elige y queda registrado el tipo de confirmación (ORIGINAL o ALTERNATIVO).'
        },
        {
          question: '¿El cliente necesita una cuenta para ver el presupuesto?',
          answer: 'No. El cliente accede al presupuesto a través de un enlace público único que se envía por email. Desde ahí puede ver el detalle completo, los precios y aprobar o rechazar sin necesidad de crear una cuenta ni iniciar sesión en el sistema.'
        },
        {
          question: '¿Cómo se registra la aprobación del cliente?',
          answer: 'Cuando el cliente aprueba desde el enlace público, el sistema registra automáticamente: el tipo de precio confirmado (original o alternativo), el canal de confirmación (email), y la fecha y hora exacta de la confirmación. El servicio asociado pasa automáticamente a estado APROBADO.'
        }
      ]
    },
    {
      id: 'ordenes',
      name: 'Órdenes de Trabajo',
      icon: 'pi pi-briefcase',
      expanded: false,
      faqs: [
        {
          question: '¿Cuál es el ciclo de vida de una orden de trabajo?',
          answer: 'Una orden de trabajo comienza en estado PENDIENTE cuando se crea desde un presupuesto aprobado. Al iniciar el trabajo, pasa a EN PROGRESO (se registra la fecha de comienzo). Al finalizar las reparaciones, pasa a TERMINADA (se registra la fecha de fin). También puede cancelarse desde el estado PENDIENTE.',
          diagram: `
            <div class="flow-diagram">
              <div class="flow-title">Ciclo de Vida de la Orden de Trabajo</div>
              <div class="flow-row">
                <div class="flow-node node-warn">PENDIENTE</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-info">EN PROGRESO</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-success">TERMINADA</div>
              </div>
              <div class="flow-branch">
                <div class="flow-branch-line" style="left: 70px;"></div>
                <div class="flow-node node-danger" style="margin-top: 0.5rem;">CANCELADA</div>
                <div class="flow-branch-label">Solo desde PENDIENTE</div>
              </div>
              <div class="flow-sync-info">
                <div class="flow-sync-title">Sincronización con el Servicio:</div>
                <div class="flow-sync-item"><span class="sync-from">Iniciar OT</span> <span class="sync-arrow">→</span> <span class="sync-to">Servicio pasa a EN REPARACIÓN</span></div>
                <div class="flow-sync-item"><span class="sync-from">Finalizar OT</span> <span class="sync-arrow">→</span> <span class="sync-to">Servicio pasa a TERMINADO</span></div>
              </div>
            </div>`
        },
        {
          question: '¿Cómo creo una orden de trabajo?',
          answer: 'La orden de trabajo se crea desde un presupuesto en estado APROBADO. Desde el detalle del presupuesto, hacé clic en "Crear Orden de Trabajo". Se copiará automáticamente los ítems del presupuesto como detalles de la orden. Solo se puede crear una orden de trabajo por presupuesto.'
        },
        {
          question: '¿Cómo asigno un técnico a la orden?',
          answer: 'Al crear la orden de trabajo se selecciona el técnico responsable de la lista de empleados disponibles. El técnico asignado será el encargado de ejecutar las reparaciones especificadas en la orden.'
        },
        {
          question: '¿Cómo funciona el seguimiento de detalles?',
          answer: 'Cada orden de trabajo contiene detalles individuales (ítems del presupuesto). El técnico puede marcar cada detalle como completado e incluir un comentario. Podés verificar si todos los detalles están completados para saber si la orden está lista para finalizar.'
        },
        {
          question: '¿Cómo funciona el tablero Kanban de órdenes?',
          answer: 'El tablero muestra las órdenes en cuatro columnas: Pendiente, En Progreso, Terminada y Cancelada. Podés arrastrar órdenes entre columnas para cambiar su estado. Al mover a EN PROGRESO se ejecuta la acción de "iniciar" (registra fecha de comienzo y cambia el servicio a EN REPARACIÓN). Al mover a TERMINADA se ejecuta "finalizar" (registra fecha de fin y cambia el servicio a TERMINADO).'
        }
      ]
    },
    {
      id: 'garantias',
      name: 'Garantías',
      icon: 'pi pi-shield',
      expanded: false,
      faqs: [
        {
          question: '¿Cómo funciona el flujo de garantía?',
          answer: 'Cuando un cliente reporta un problema con una reparación previa, se crea un servicio de garantía vinculado al servicio original. El servicio ingresa en estado ESPERANDO EVALUACIÓN GARANTÍA. Un técnico evalúa si el problema está cubierto y decide entre tres opciones: la garantía cumple (se repara sin costo), la garantía no cumple (rechazada), o el equipo no tiene reparación posible.',
          diagram: `
            <div class="flow-diagram">
              <div class="flow-title">Flujo de Servicio en Garantía</div>
              <div class="flow-row">
                <div class="flow-node node-info">RECIBIDO</div>
                <div class="flow-arrow"></div>
                <div class="flow-node node-warn">ESPERANDO<br>EVALUACIÓN<br>GARANTÍA</div>
              </div>
              <div class="flow-evaluation">
                <div class="flow-eval-branch">
                  <div class="flow-eval-label eval-success">CUMPLE</div>
                  <div class="flow-arrow-down"></div>
                  <div class="flow-node node-info">EN REPARACIÓN</div>
                  <div class="flow-arrow-down"></div>
                  <div class="flow-node node-success">TERMINADO</div>
                  <div class="flow-arrow-down"></div>
                  <div class="flow-node node-success">FINALIZADO</div>
                </div>
                <div class="flow-eval-branch">
                  <div class="flow-eval-label eval-danger">NO CUMPLE</div>
                  <div class="flow-arrow-down"></div>
                  <div class="flow-node node-danger">GARANTÍA<br>RECHAZADA</div>
                </div>
                <div class="flow-eval-branch">
                  <div class="flow-eval-label eval-secondary">SIN REPARACIÓN</div>
                  <div class="flow-arrow-down"></div>
                  <div class="flow-node node-secondary-dark">GARANTÍA SIN<br>REPARACIÓN</div>
                </div>
              </div>
              <div class="flow-note">No se genera presupuesto. La orden de trabajo se crea sin costo.</div>
            </div>`
        },
        {
          question: '¿Cómo creo un servicio de garantía?',
          answer: 'Desde el detalle de un servicio finalizado, podés crear un servicio de garantía usando la opción correspondiente. El sistema vinculará automáticamente el nuevo servicio con el servicio original y lo marcará como garantía. No se genera presupuesto para servicios de garantía.'
        },
        {
          question: '¿Cómo se evalúa la garantía?',
          answer: 'Desde el detalle del servicio en estado ESPERANDO EVALUACIÓN GARANTÍA, el técnico puede ver los ítems del servicio original y seleccionar cuáles están cubiertos. La evaluación tiene tres resultados posibles: CUMPLE (se crea orden de trabajo sin costo con los ítems seleccionados), NO CUMPLE (garantía rechazada), o SIN REPARACIÓN (equipo no reparable bajo garantía).'
        },
        {
          question: '¿Qué diferencia tiene una orden de trabajo de garantía?',
          answer: 'Las órdenes de trabajo de garantía se crean con la marca "Sin Costo" (esSinCosto = true) y no tienen presupuesto asociado. Los ítems se toman de la evaluación de garantía en lugar de un presupuesto. El flujo posterior (iniciar, completar detalles, finalizar) es igual al de una orden normal.'
        }
      ]
    },
    {
      id: 'clientes',
      name: 'Gestión de Clientes',
      icon: 'pi pi-users',
      expanded: false,
      faqs: [
        {
          question: '¿Cómo registro un nuevo cliente?',
          answer: 'Desde la sección "Clientes", hacé clic en "Nuevo Cliente". Completá el formulario con la información requerida: nombre, apellido, tipo y número de documento, teléfono y email. Los campos marcados con asterisco (*) son obligatorios.'
        },
        {
          question: '¿Qué información del cliente es obligatoria?',
          answer: 'Los campos obligatorios son: Nombre, Apellido, Tipo de Documento y Número de Documento. El teléfono y email son opcionales pero muy recomendados para mantener comunicación con el cliente, especialmente el email que se utiliza para enviar presupuestos.'
        },
        {
          question: '¿Puedo ver el historial de servicios de un cliente?',
          answer: 'Sí, al hacer clic en un cliente desde la lista, accedés a su perfil detallado donde podés ver todos sus servicios, equipos registrados y presupuestos asociados.'
        },
        {
          question: '¿Cómo busco un cliente específico?',
          answer: 'Usá el campo de búsqueda en la parte superior de la lista de clientes. Podés buscar por nombre, apellido, documento o email. El sistema filtra automáticamente mientras escribís.'
        }
      ]
    },
    {
      id: 'equipos',
      name: 'Gestión de Equipos',
      icon: 'pi pi-desktop',
      expanded: false,
      faqs: [
        {
          question: '¿Cómo registro un equipo nuevo?',
          answer: 'Desde la sección "Equipos", hacé clic en "Nuevo Equipo". Seleccioná el cliente propietario, el tipo de equipo, marca y modelo. También podés agregar el número de serie y observaciones adicionales sobre el equipo.'
        },
        {
          question: '¿Qué es el número de serie y para qué sirve?',
          answer: 'El número de serie es un identificador único del equipo asignado por el fabricante. Es útil para la garantía del fabricante y para identificar el equipo de manera precisa. Aunque es opcional, se recomienda registrarlo siempre que sea posible.'
        },
        {
          question: '¿Puedo ver qué servicios se han realizado a un equipo?',
          answer: 'Sí, en el detalle del equipo encontrás el historial completo de servicios realizados, incluyendo reparaciones, presupuestos y órdenes de trabajo asociadas a ese equipo específico.'
        },
        {
          question: '¿Cómo agrego nuevos tipos de equipo, marcas o modelos?',
          answer: 'Los usuarios con permisos de administrador pueden agregar nuevos tipos de equipo, marcas y modelos desde la sección de "Equipos" utilizando los botones de gestión de catálogos. Desde ahí se pueden crear, editar o eliminar elementos de los catálogos.'
        }
      ]
    },
    {
      id: 'empleados',
      name: 'Gestión de Empleados',
      icon: 'pi pi-id-card',
      expanded: false,
      faqs: [
        {
          question: '¿Cómo registro un nuevo empleado?',
          answer: 'Desde la sección "Empleados", hacé clic en "Nuevo Empleado". Completá el formulario con los datos personales e información de contacto del empleado.'
        },
        {
          question: '¿Cuál es la diferencia entre empleado y usuario?',
          answer: 'Un empleado es una persona que trabaja en el taller. Un usuario es una cuenta de acceso al sistema. Un empleado puede tener o no un usuario asociado. Solo los empleados con un usuario creado pueden acceder al sistema.'
        },
        {
          question: '¿Cómo le creo acceso al sistema a un empleado?',
          answer: 'Desde el detalle del empleado, podés crear un usuario asociado. Definí el nombre de usuario, email y contraseña inicial. El empleado podrá usar estas credenciales para acceder al sistema según los permisos de su rol.'
        }
      ]
    },
    {
      id: 'usuarios',
      name: 'Gestión de Usuarios',
      icon: 'pi pi-user',
      expanded: false,
      faqs: [
        {
          question: '¿Qué roles existen en el sistema?',
          answer: 'El sistema tiene tres roles: PROPIETARIO (acceso completo al sistema, puede gestionar usuarios y configuración), ADMINISTRATIVO (gestión de clientes, servicios, presupuestos y órdenes de trabajo), y TÉCNICO (gestión de reparaciones, órdenes de trabajo asignadas y evaluación de garantías).'
        },
        {
          question: '¿Cómo restablezco la contraseña de un usuario?',
          answer: 'Los usuarios con rol Propietario pueden restablecer contraseñas desde la gestión de usuarios. Seleccioná el usuario y usá la opción correspondiente para establecer una nueva contraseña.'
        },
        {
          question: '¿Puedo desactivar un usuario sin eliminarlo?',
          answer: 'Sí, podés cambiar el estado del usuario a inactivo. Esto evitará que pueda acceder al sistema sin perder el historial de acciones realizadas por ese usuario.'
        }
      ]
    },
    {
      id: 'general',
      name: 'General',
      icon: 'pi pi-info-circle',
      expanded: false,
      faqs: [
        {
          question: '¿Cómo se sincronizan los estados entre entidades?',
          answer: 'El sistema mantiene sincronizados los estados entre Servicios, Presupuestos y Órdenes de Trabajo de forma automática. Al crear un presupuesto, el servicio pasa a PRESUPUESTADO. Al aprobar un presupuesto, el servicio pasa a APROBADO. Al iniciar una orden de trabajo, el servicio pasa a EN REPARACIÓN. Al finalizar una orden, el servicio pasa a TERMINADO.',
          diagram: `
            <div class="flow-diagram">
              <div class="flow-title">Sincronización entre Entidades</div>
              <div class="flow-sync-table">
                <div class="sync-header">
                  <div class="sync-col">Acción</div>
                  <div class="sync-col">Presupuesto</div>
                  <div class="sync-col">Servicio</div>
                </div>
                <div class="sync-row">
                  <div class="sync-col sync-action">Crear presupuesto</div>
                  <div class="sync-col"><span class="sync-badge badge-warn">PENDIENTE</span></div>
                  <div class="sync-col"><span class="sync-badge badge-warn">PRESUPUESTADO</span></div>
                </div>
                <div class="sync-row">
                  <div class="sync-col sync-action">Aprobar presupuesto</div>
                  <div class="sync-col"><span class="sync-badge badge-success">APROBADO</span></div>
                  <div class="sync-col"><span class="sync-badge badge-info">APROBADO</span></div>
                </div>
                <div class="sync-row">
                  <div class="sync-col sync-action">Rechazar presupuesto</div>
                  <div class="sync-col"><span class="sync-badge badge-danger">RECHAZADO</span></div>
                  <div class="sync-col"><span class="sync-badge badge-danger">RECHAZADO</span></div>
                </div>
              </div>
              <div class="flow-sync-table" style="margin-top: 1rem;">
                <div class="sync-header">
                  <div class="sync-col">Acción</div>
                  <div class="sync-col">Orden de Trabajo</div>
                  <div class="sync-col">Servicio</div>
                </div>
                <div class="sync-row">
                  <div class="sync-col sync-action">Iniciar OT</div>
                  <div class="sync-col"><span class="sync-badge badge-info">EN PROGRESO</span></div>
                  <div class="sync-col"><span class="sync-badge badge-info">EN REPARACIÓN</span></div>
                </div>
                <div class="sync-row">
                  <div class="sync-col sync-action">Finalizar OT</div>
                  <div class="sync-col"><span class="sync-badge badge-success">TERMINADA</span></div>
                  <div class="sync-col"><span class="sync-badge badge-success">TERMINADO</span></div>
                </div>
              </div>
            </div>`
        },
        {
          question: '¿El sistema envía notificaciones?',
          answer: 'Sí, el sistema utiliza notificaciones en tiempo real mediante WebSocket. Los usuarios conectados reciben actualizaciones instantáneas cuando se crean, actualizan o cambian de estado los servicios, presupuestos y órdenes de trabajo. Esto mantiene sincronizados los tableros Kanban sin necesidad de recargar la página.'
        },
        {
          question: '¿Cómo funciona la firma digital?',
          answer: 'El sistema captura firmas digitales en dos momentos: la firma de ingreso (cuando el cliente deja el equipo en el taller) y la firma de conformidad (cuando el cliente retira el equipo reparado). Las firmas se capturan dibujando con el mouse o el dedo en un panel táctil y se almacenan de forma segura en el sistema.'
        }
      ]
    }
  ]);

  selectedModule = computed(() => {
    return this.modules().find(m => m.id === this.selectedModuleId());
  });

  selectModule(moduleId: string): void {
    this.selectedModuleId.set(moduleId);
  }

  toggleModule(moduleId: string): void {
    this.modules.update(modules =>
      modules.map(m =>
        m.id === moduleId
          ? { ...m, expanded: !m.expanded }
          : m
      )
    );
  }

  sanitizeDiagram(html: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
