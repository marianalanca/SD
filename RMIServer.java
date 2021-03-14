// É o servidor central (replicado) que armazena todos os dados da aplicação, suportando por essa razão todas as operações necessárias através demétodos remotos usando Java RMI
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
/*Por os restantes objects que podem ser passados */ 
public class RMIServer extends UnicastRemoteObject implements  {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public RMIServer() throws RemoteException{
            super();
      }

      public static void main(String[] args) {
            System.getProperties().put("java.security.policy", "policy.all");
            
            try () {
                  RMIServer rmiServer = new RMIServer();
                  LocateRegistry.createRegistry(5001).rebind("Election", RMIServer);
            } catch (Exception e) {
                  //TODO: handle exception
            }
      }

      


}
