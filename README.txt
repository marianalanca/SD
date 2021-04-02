CORRER O SOFTWARE eVOTING
	Os passos devem ser seguidos por ordem.

	Correr o Servidor RMI:
		java -jar RMIServer.jar

	Correr a consola de administração
		java -jar AdminConsole.jar

	Correr o servidor Multicast (podem ser abertos tantos servidores quantos os departamentos)
		java -jar MulticastServer.jar <departamento>

	Correr o cliente Multicast (podem ser abertos tantos clientes quanto os desejados)
		java -jar MulticastClient.jar <departamento>
