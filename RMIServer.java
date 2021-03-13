// É o servidor central (replicado) que armazena todos os dados da aplicação, suportando por essa razão todas as operações necessárias através demétodos remotos usando Java RMI
import java.rmi.*;

public interface RMIServer extends Remote {
}
