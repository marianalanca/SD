CORRER O SOFTWARE eVOTING
	Os passos devem ser seguidos por ordem.

	Compilar o projeto (este comando imprimirá alguns warnings)
		javac *.java

	Correr o Servidor RMI:
		java RMIServer

	Correr a consola de administração
		java AdminConsole

	Correr o servidor Multicast (podem ser abertos tantos servidores quantos os departamentos)
		java MulticastServer <departamento>

	Correr o cliente Multicast (podem ser abertos tantos clientes quanto os desejados)
		java MulticastClient <departamento>
