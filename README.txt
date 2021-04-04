***CORRER O SOFTWARE eVOTING***
	Os passos devem ser seguidos por ordem.

	1. Correr o Servidor RMI:
		java -jar RMIServer.jar

	2. Correr a consola de administração
		java -jar AdminConsole.jar

	3. Correr o servidor Multicast (podem ser abertos tantos servidores quantos os departamentos)
		java -jar MulticastServer.jar <departamento>

	4. Correr o cliente Multicast (podem ser abertos tantos clientes quanto os desejados)
		java -jar MulticastClient.jar <departamento>

CRIAR UM NOVO VOTER
	Nota: É obrigatório o Servidor RMI estar ligado

	1. Na AdminConsole, escolher a opção 1
	2. Inserir os dados pedidos: Nome, role (Student, Docente ou funcionario), CC, data de expiração (tem de ser superior à atual), departamento, contacto, endereço e password
	3. No final, terá sido criado um novo voter


CRIAR UMA NOVA ELEIÇÃO
	Nota: É obrigatório o Servidor RMI estar ligado

	1. Na AdminConsole, escolher a opção 3
	2. Inserir os dados pedidos: Nome de eleição, escolher se é geral ou simples (no segundo caso inserir o tipo - Student, Docente ou funcionario), departamento, descrição e data fim e de início (ambas devem ser posteriores à data e hora atual)
	3. Escolher a opção 6 (Manage polling stations) e selecionar 1 para associar a eleição a um departamento (é necessário ter uma mesa aberta com o departamento)
	4. Escolher a opção 5 e selecionar a eleição desejada para adicionar uma nova lista e/ou candidato
	5. No final, a eleição estará criada

VOTAR
	Nota: É obrigatório o Servidor RMI estar ligado
		  É necessário ter o multicastServer com um departamento associado a alguma eleição e um ou mais terminais

	1. No servidor multicast, selecionar a opção 1, escolher o modo de autenticação e inserir os dados corretos
	2. Se algum terminal estiver disponível no momento, este será desbloqueado e surgirá um campo para inserir o username e password
	3. Após o login, escolher uma das eleições disponíveis (se já tiver votado em todas as eleições disponíveis ou se não existirem, surge uma mensagem e faz-se logout)
	4. Escolher um dos candidatos para VOTAR
	5. Surgirá uma mensagem a indicar o sucesso ou insucesso da submissão do voto