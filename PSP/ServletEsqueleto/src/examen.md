# Practica Servlets, Sessiones y demas

Se pide hacer una web para guardar en una caja fuerte unos secretos, los secretos son por usuario. 
como es una primera version los secretos solo duran lo que el usuario esté delante de un navegador.
Se pide lo siguiente:

1. cuando el usuario abre el navegador, guarda tres números que son la combinacion de su caja fuerte, los guarda en una única llamada,
no hace falta guardar el nombre del usuario. Haz un servlet que haga esto.
2. Para abrir su caja fuerte en el navegador tiene que ir metiendo los numeros en orden, uno por llamada, si los números son
1,2,3 en la primera llamada pasará el 1, en la segunda el 2, en la tercera el 3, cualquier fallo vuelve la secuencia al principio, 
Si mete 1, y luego 4, la caja fuerte esperará un 1. Haz un servlet que haga esto.

3. Una vez abierta la caja fuerte, podrá guardar los secretos, haz un servlet que guarde secretos, un nombre y un valor.
4. Haz otro servlet que consulte los secretos, por nombre.

### Gestiona los siguientes errores:
- alguien llama a los puntos 2,3,4 sin haber configurado la caja.
- Controla errores de tipos de parametros.


