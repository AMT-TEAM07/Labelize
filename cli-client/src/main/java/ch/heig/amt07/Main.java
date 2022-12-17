package ch.heig.amt07;

public class Main {
    public static void main(String[] args) {
        // Scénario 1: Le bucket et l'objet n'existent pas
        try {
            LabelizeClient.runScenarioOne();
        } catch (Exception e) {
            System.out.println("Le scénario 1 a échoué avec l'excpetion suivante: " + e.getMessage());
            return;
        }

        // Scénario 2: Le bucket existe, mais pas l'objet
        try {
            LabelizeClient.runScenarioTwo();
        } catch (Exception e) {
            System.out.println("Le scénario 2 a échoué avec l'excpetion suivante: " + e.getMessage());
            return;
        }

        // Scénario 3: Le bucket et l'objet existent
        try {
            LabelizeClient.runScenarioTwo();
        } catch (Exception e) {
            System.out.println("Le scénario 3 a échoué avec l'excpetion suivante: " + e.getMessage());
        }
    }

}