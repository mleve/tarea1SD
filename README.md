tarea1SD
========

Tarea 1 de sistemas distribuidos

Clase PacMan extiende de jFrame y lo unico que hace es agregar un JPanel (board)
y setear una que otra propiedad.

	Prop1: Dejar board en servidor, y enviarla a clientes PacMan en cuanto inicien
		=>problemas: JPanel no es serializable (lo + probable).
					 Si se puede, se enviara una copia de board a los clientes, 
					 es decir, cada uno vera su propio tablero personal. Podria
					 hacerse que los clientes manden sus "boards" luego de mover
					 para sincronizar el servidor, pero seria estupidamente 
					 ineficiente.

No es recomendado transmitir ( y por ende serializar elementos de la GUI), mejor
separar el codigo y solo enviar variables o datos.

Que datos mantener centralizados:
	-Estado del tablero
	-posicion de(los) pacman(s) 
	-posicion de los fantasmas
	-Vidas de cada pacman


Que deberia hacer cada cliente:
	-Tener todo el codigo para la creacio de las interfaces graficas
	-solicitar al servidor el "estado del juego" y luego dibujar/refrescar
	 el estado del mismo en la interface grafica.


Información que deberia pasar por el socket:
	-Al inicio cliente pide conexion a servidor
		recibe: identificador (int), estado completo del juego.
	-Solicitud de movimiento de cliente a servidor
		recibo: vidas restantes, puntaje, posiciones pacmans, posiciones ghosts,
			estado tablero.

Como funciona actualmente:
	Se setea un timer de 40 milisegundos, cuando se cumple, se redibuja el JPanel 
tablero