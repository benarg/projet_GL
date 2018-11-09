    public class TestZoo {

    public static void main(String[] args) {
        testZooHeritage();
    }

    private static void testZooHeritage() {
        Zoo ensimag = new Zoo("Ensimag avec Heritage");
        ensimag.ajouteAnimal(new Canard());
        ensimag.ajouteAnimal(new Vache());
        ensimag.ajouteAnimal(new Chien());
        ensimag.ajouteAnimal(new Canard());
        ensimag.ajouteAnimal(new Vache());
        ensimag.ajouteAnimal(new Chien());

        ensimag.crier();
    }
}
