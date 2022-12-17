import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MainTest {

    @BeforeEach
    void setup() {
        System.out.println("setUp");
    }

    @Disabled("to not spam bucket manipulation")
    @Test
    void Scenario_One_Nothing_Exists() {
        //given

        //when

        //then
        assert(true);
    }

    @Disabled("Not implemented yet")
    @Test
    void Scenario_Two_Only_RootObject_Exists() {
        //given

        //when

        //then
    }

    @Disabled("Not implemented yet")
    @Test
    void Scenario_Three_Everything_Exists() {
        //given

        //when

        //then
    }

    @AfterEach
    void teardown() {
        System.out.println("tearDown");
    }
}
