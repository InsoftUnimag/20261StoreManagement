# Actores del módulo 1

### Operario de recepción
Este actor sera quien reciba el abastecimiento en el centro de distribución. Sus tareas son:

* Registrar el producto en el sistema (Tipo, Cantidad, Fecha de vencimiento, nombre, etc).

### Supervisor de inventario
Este actor sera quien se encargue de verificar el estado del producto dentro del centro de distribución. Sus tareas son:

* Notificar en el sistema daños en el producto (Averia, vencimiento).

### Operario de despacho
Este actor se encarga de ofrecer información sobre la entrega del producto a la logistica de despacho y transporte. Sus tareas son:

* Informar si el pedido ha sido despachado exitosamente.

### Cliente
Este actor se encarga de realizar pedidos de distintos productos que ofrece el centro de distibución. Sus tareas son:

* Refistrarse en el sistema como usuario consumidor.
* Realizar un pedido (distintos tipos de producto y cantidades).

### Módulo de logística de despacho
Este actor se encarga de ofrecer una ruta de entrega a los pedidos. Sus tareas son:

* Ofrecer rutas de entrega en diferentes municipios.
* Confirmar una ruta de entrega para uno o varios pedidos (que tengan una ruta en común).

### Módulo de logística de finanzas
Este actor se encarga de realizar los cobros a los clientes por sus pedidos. Sus tareas son:

* Solicitar información de un pedido (Cliente que recibe, contenido del pedido, precio del pedido).