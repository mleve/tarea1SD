tarea1SD
========
#MARIO

Movi el calculo de las posiciones de los fantasmas al servidor, esto implico que ahora el servidor tiene un "ciclo" de refresco (implementado de
la misma forma que para los clientes, con un timer de 40 miliseg y un ActionListener. Cada 40 miliseg se actualiza la posicion de los fantasmas.

Ahora los clientes, en vez de hacer este calculo en moveGhost, piden las posiciones de los fantasmas (en 2 llamadas, para mantener la estructura
del programa original)

Ademas, me di cuenta que el arreglo screendata es el que guarda la información del tablero (y lo importante, donde quedan puntos para comer) de forma que, a modo de prueba, implemente que los clientes envien su screendata al servidor luego de mover su Pacman (metodo movePacMan() ), que es
la unica parte donde este arreglo es modificado.

Ademas, al momento de dibujar el tablero, los clientes "sincronizan" su screendata con la del servidor (en realidad, reemplazan uno por otro)

COn estos cambios, ahora los clientes sincronizan las posiciones de fantasmas y del tablero, aunque esta ultima sufre
bastante con los efectos del LAG.

Convendria hacer que en vez de pasar todo el arreglo screendata por RMI, solo mandar los cambios (para el caso de enviar la info al servidor es 
facil, en MovePacMan(), cuando se modifica screendata, habria que enviar ese cambio tambien al servidor), para el otro caso, creo que no hay muchas mas opciones que enviar todo el arreglo :S (quizas convenga que esta actualizacion no sea cada 40 mseg)

#LUIS

Implemente lo basico del servidor y algunas llamadas del cliente hacia el servidor.  
Creo que la informacion que debe mantener el servidor debe ser la justa y necesaria para que cada jugador sepa:  
	La posicion de cada pacman  
	La posicion de cada fantasma  
	El estado mas basico de los otros jugadores (listo, jugando, muerto)  
	Las fichas que han sido comidas  
  
El servidor es solo para sincronizacion y no para detectar cheaters. Por lo tanto no es necesario saber si un jugador esta en un area permitida del tablero o si esta atravesando una pared, por eso no necesita conocer el tablero.  
Toda la inteligencia esta en el cliente y el servidor confia en la informacion que el cliente le envia.  
El servidor no necesita conocer el tablero, sino que solamente almacena la informacion que los clientes le envian, las sincroniza y la reparte a los demas jugadores. Es el cliente el que se encarga de saber si el pacman propio esta muerto y comunicarselo al servidor.  
  
Las fichas se pueden representar como un array unidimensional, es decir, cada ficha tiene un indice. Cuando el cliente come una ficha se lo informa al servidor. Si este cliente es el primero en comer la ficha (siempre hay un primero porque las llamadas estan encoladas) entonces el servidor marca la ficha en su array como ya comida.  
En cada llamada del cliente al servidor en que el cliente consulta la posicion de los demas pacman y fantasmas, ademas, se le envia como respuesta el array de fichas que mantiene el servidor. Si enviar este array es muy pesado y toma mucho tiempo, se puede enviar solamente una actualizacion del estado de fichas: se envia los indices de las fichas comidas (por el y por otros) durante el intervalo de tiempo desde su ultima consulta. Como la entrega de respuestas de RMI estan aseguradas, creo, porque se transmiten por TCP, entonces siempre esta informacion va a ser correcta y suficiente para cada cliente.  
  
Como se consulta al servidor en cada repaint(), (40ms) se podria poner un timeout en el servidor por el que se elimine a un jugador si su tablero no realiza consultas al servidor por mas de 1 o 2 o 3... segundos.  
  
Cree mi branch (lucho) e hice las modificaciones a partir del original. Luego le hice merge con master.  
Mario, movi tus cambios a tu branch (mario) para que no se pierdan.  

#MARIO

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
