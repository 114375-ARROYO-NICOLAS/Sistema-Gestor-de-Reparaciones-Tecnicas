import { Component, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

interface FAQ {
  question: string;
  answer: string;
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
  selectedModuleId = signal<string>('clientes');

  modules = signal<Module[]>([
    {
      id: 'clientes',
      name: 'Gestión de Clientes',
      icon: 'pi pi-users',
      expanded: false,
      faqs: [
        {
          question: '¿Cómo registro un nuevo cliente?',
          answer: 'Para registrar un nuevo cliente, ve a la sección "Clientes" y haz clic en el botón "Nuevo Cliente". Completa el formulario con la información requerida: nombre, apellido, documento, teléfono y email. Los campos marcados con asterisco (*) son obligatorios.'
        },
        {
          question: '¿Qué información del cliente es obligatoria?',
          answer: 'Los campos obligatorios son: Nombre, Apellido, Tipo de Documento y Número de Documento. El teléfono y email son opcionales pero muy recomendados para mantener comunicación con el cliente.'
        },
        {
          question: '¿Puedo ver el historial de servicios de un cliente?',
          answer: 'Sí, al hacer clic en un cliente desde la lista, accederás a su perfil detallado donde podrás ver todos sus servicios previos, equipos registrados, presupuestos y órdenes de trabajo asociadas.'
        },
        {
          question: '¿Cómo busco un cliente específico?',
          answer: 'Utiliza el campo de búsqueda en la parte superior de la lista de clientes. Puedes buscar por nombre, apellido, documento o email. El sistema filtrará automáticamente los resultados mientras escribes.'
        },
        {
          question: '¿Puedo editar la información de un cliente?',
          answer: 'Sí, desde el detalle del cliente, haz clic en el botón "Editar" para modificar su información. Recuerda guardar los cambios antes de salir de la pantalla.'
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
          answer: 'Desde la sección "Equipos", haz clic en "Nuevo Equipo". Selecciona el cliente propietario, el tipo de equipo, marca y modelo. También puedes agregar el número de serie y observaciones adicionales sobre el equipo.'
        },
        {
          question: '¿Qué es el número de serie y para qué sirve?',
          answer: 'El número de serie es un identificador único del equipo asignado por el fabricante. Es muy útil para la garantía del fabricante y para identificar el equipo de manera precisa. Aunque es opcional, se recomienda registrarlo siempre que sea posible.'
        },
        {
          question: '¿Puedo ver qué servicios se han realizado a un equipo?',
          answer: 'Sí, en el detalle del equipo encontrarás el historial completo de servicios realizados, incluyendo reparaciones, presupuestos y órdenes de trabajo asociadas a ese equipo específico.'
        },
        {
          question: '¿Cómo agrego nuevos tipos de equipo, marcas o modelos?',
          answer: 'Los usuarios con permisos de administrador pueden agregar nuevos tipos de equipo, marcas y modelos desde el menú "Configuración". Allí encontrarás las secciones específicas para gestionar estos catálogos.'
        }
      ]
    },
    {
      id: 'servicios',
      name: 'Servicios',
      icon: 'pi pi-wrench',
      expanded: false,
      faqs: [
        {
          question: '¿Cuál es el flujo completo de un servicio?',
          answer: 'Un servicio inicia cuando se recibe un equipo (estado RECIBIDO), luego pasa a diagnóstico (EN_DIAGNOSTICO), se genera un presupuesto que el cliente puede APROBAR o RECHAZAR. Si se aprueba, se crea una orden de trabajo y el servicio pasa a EN_REPARACION, luego a REPARADO y finalmente a ENTREGADO cuando el cliente retira el equipo.'
        },
        {
          question: '¿Cómo creo un nuevo servicio?',
          answer: 'Haz clic en "Nuevo Servicio", selecciona o crea el cliente y su equipo. Describe el problema reportado en "Descripción del problema" y agrega cualquier observación inicial. El servicio se creará en estado RECIBIDO automáticamente.'
        },
        {
          question: '¿Qué hago cuando termino el diagnóstico?',
          answer: 'Una vez completado el diagnóstico, desde el detalle del servicio puedes crear un presupuesto. Describe los trabajos a realizar, agrega los repuestos necesarios y el costo de mano de obra. El presupuesto se enviará al cliente para su aprobación.'
        },
        {
          question: '¿Puedo modificar un servicio una vez creado?',
          answer: 'Sí, puedes editar la información del servicio y cambiar su estado según avance el proceso. Sin embargo, algunos cambios pueden estar restringidos dependiendo del estado actual del servicio para mantener la integridad de los datos.'
        },
        {
          question: '¿Cómo busco un servicio específico?',
          answer: 'Utiliza la función de búsqueda en la sección "Servicios - Buscar". Puedes filtrar por número de servicio, cliente, equipo, estado o rango de fechas para encontrar rápidamente el servicio que necesitas.'
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
          question: '¿Cómo creo un presupuesto?',
          answer: 'Los presupuestos se crean desde el detalle de un servicio después de realizar el diagnóstico. Haz clic en "Crear Presupuesto", describe los trabajos a realizar, agrega los repuestos con sus precios y especifica el costo de mano de obra.'
        },
        {
          question: '¿Cómo envío el presupuesto al cliente?',
          answer: 'Una vez creado el presupuesto, el sistema genera automáticamente un enlace público único. Puedes enviar este enlace al cliente por email o WhatsApp. El cliente podrá ver el detalle y aprobar o rechazar el presupuesto desde ese enlace sin necesidad de ingresar al sistema.'
        },
        {
          question: '¿Qué pasa si el cliente rechaza el presupuesto?',
          answer: 'Si el cliente rechaza el presupuesto, el servicio queda en un estado que permite generar un nuevo presupuesto con modificaciones, o proceder a la devolución del equipo según lo acordado con el cliente.'
        },
        {
          question: '¿Puedo modificar un presupuesto después de crearlo?',
          answer: 'Puedes modificar un presupuesto solo si aún no ha sido aprobado por el cliente. Una vez aprobado, se genera automáticamente una orden de trabajo y el presupuesto queda bloqueado para mantener el registro de lo acordado.'
        },
        {
          question: '¿El cliente necesita una cuenta para ver el presupuesto?',
          answer: 'No, el cliente puede ver y responder al presupuesto a través de un enlace público único, sin necesidad de crear una cuenta o iniciar sesión en el sistema.'
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
          question: '¿Cuándo se crea una orden de trabajo?',
          answer: 'Una orden de trabajo se crea automáticamente cuando el cliente aprueba un presupuesto. La orden contiene todos los detalles del trabajo a realizar, repuestos necesarios y costos acordados.'
        },
        {
          question: '¿Cómo asigno una orden de trabajo a un técnico?',
          answer: 'Desde el detalle de la orden de trabajo, puedes asignar el técnico responsable seleccionándolo de la lista de empleados disponibles. El técnico asignado será el encargado de ejecutar las reparaciones especificadas.'
        },
        {
          question: '¿Puedo agregar repuestos adicionales a una orden de trabajo?',
          answer: 'Si durante la reparación se identifican repuestos adicionales necesarios que no estaban en el presupuesto original, deberás crear un presupuesto adicional para que el cliente lo apruebe antes de proceder.'
        },
        {
          question: '¿Cómo marco una orden de trabajo como completada?',
          answer: 'Una vez finalizadas las reparaciones, actualiza el estado de la orden a COMPLETADA. Agrega cualquier observación final sobre el trabajo realizado. Esto actualizará automáticamente el estado del servicio asociado.'
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
          question: '¿Cómo funciona el sistema de garantías?',
          answer: 'Cuando se completa un servicio de reparación, se puede asignar un período de garantía (por ejemplo, 90 días). Durante este período, si el cliente reporta un problema relacionado con la reparación realizada, puede activarse un servicio en garantía sin costo adicional.'
        },
        {
          question: '¿Cómo ingreso un servicio en garantía?',
          answer: 'Cuando un cliente trae un equipo con una reparación bajo garantía, crea un nuevo servicio y marca la opción "Servicio en Garantía". El sistema vinculará automáticamente este servicio con el servicio original cubierto por la garantía.'
        },
        {
          question: '¿Qué pasa si el problema no está cubierto por la garantía?',
          answer: 'Puedes evaluar si el problema está relacionado con la reparación original o es un problema nuevo. Si no está cubierto por garantía, puedes convertir el servicio en uno regular y proceder con un nuevo diagnóstico y presupuesto.'
        },
        {
          question: '¿Dónde veo todas las garantías vigentes?',
          answer: 'En la sección "Garantías" encontrarás un tablero con todos los servicios que tienen garantía activa, su fecha de vencimiento y estado actual. Puedes filtrar por garantías próximas a vencer o por cliente.'
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
          answer: 'Desde la sección "Empleados", haz clic en "Nuevo Empleado". Completa el formulario con los datos personales, información de contacto y rol del empleado (TECNICO, RECEPCIONISTA, ADMINISTRADOR, etc.).'
        },
        {
          question: '¿Cuál es la diferencia entre empleado y usuario?',
          answer: 'Un empleado es una persona que trabaja en el taller (técnico, recepcionista, etc.). Un usuario es una cuenta de acceso al sistema. Un empleado puede tener o no un usuario asociado. Solo los empleados con usuario pueden acceder al sistema.'
        },
        {
          question: '¿Cómo le creo acceso al sistema a un empleado?',
          answer: 'Desde el detalle del empleado, puedes crear un usuario asociado. Define el nombre de usuario, email y contraseña inicial. El empleado podrá usar estas credenciales para acceder al sistema según los permisos de su rol.'
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
          answer: 'El sistema maneja diferentes roles: ADMINISTRADOR (acceso completo), TECNICO (gestión de servicios y reparaciones), RECEPCIONISTA (atención al cliente y servicios), y otros roles personalizados según las necesidades del taller.'
        },
        {
          question: '¿Cómo restablezco la contraseña de un usuario?',
          answer: 'Los administradores pueden restablecer contraseñas desde la gestión de usuarios. Selecciona el usuario y usa la opción "Restablecer contraseña" para generar una nueva contraseña temporal que el usuario deberá cambiar en su primer inicio de sesión.'
        },
        {
          question: '¿Puedo desactivar un usuario sin eliminarlo?',
          answer: 'Sí, puedes cambiar el estado del usuario a "Inactivo". Esto evitará que pueda acceder al sistema sin perder el historial de acciones realizadas por ese usuario.'
        }
      ]
    },
    {
      id: 'configuracion',
      name: 'Configuración',
      icon: 'pi pi-cog',
      expanded: false,
      faqs: [
        {
          question: '¿Qué puedo configurar en el sistema?',
          answer: 'Desde el menú de configuración puedes gestionar los catálogos del sistema: tipos de equipo (computadoras, impresoras, etc.), marcas (HP, Dell, Samsung, etc.) y modelos específicos. También puedes configurar otros parámetros del sistema según tus permisos.'
        },
        {
          question: '¿Quién puede acceder a la configuración?',
          answer: 'Solo los usuarios con rol de ADMINISTRADOR tienen acceso completo a las opciones de configuración. Esto previene cambios accidentales o no autorizados en los catálogos y parámetros del sistema.'
        },
        {
          question: '¿Puedo eliminar un tipo de equipo, marca o modelo?',
          answer: 'Puedes eliminar elementos de los catálogos solo si no están siendo utilizados en ningún equipo registrado. Si hay equipos asociados, el sistema te impedirá eliminar el elemento para mantener la integridad de los datos.'
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
}
