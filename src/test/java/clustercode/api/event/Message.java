package clustercode.api.event;

public class Message {

        private int value;

        void increment() {
            this.value += 1;
        }

        int getValue() {
            return value;
        }

}
