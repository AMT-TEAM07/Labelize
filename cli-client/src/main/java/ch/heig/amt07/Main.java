package ch.heig.amt07;

public class Main {
    public static void main(String[] args) {
        var labelizer = new LabelizeClient();

        // Scénario 1 : Le bucket et l'objet n'existent pas (Commenté pour éviter de créer des buckets à chaque fois)
        labelizer.runScenarioOne();

        // Scénario 2 : Le bucket existe, mais pas l'objet
        labelizer.runScenarioTwo();

        // Scénario 3 : Le bucket et l'objet existent
        labelizer.runScenarioThree();
    }
}