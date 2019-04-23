//#include <QtWidgets>
//#include <UI/PlayerInfoUI.h>
#include <cpprest/ws_client.h>
#include <cpprest/producerconsumerstream.h>
using namespace web;
using namespace web::websockets::client;
using namespace std;

int main(int argc, char *argv[])
{

    websocket_client client;
    auto task = client.connect(U("wss://localhost:9001/ws"));
    task.wait();

    /*
    websocket_outgoing_message msg;
    concurrency::streams::producer_consumer_buffer<uint8_t> buf;
    std::vector<uint8_t> body(6);
    memcpy(&body[0], "hello!", 6);

    auto send_task = buf.putn_nocopy(&body[0], body.size()).then([&](size_t length) {
        msg.set_binary_message(buf.create_istream(), length);
        return client.send(msg);
    }).then([](pplx::task<void> t)
            {

            });
    send_task.wait();

    auto receive_task = client.receive().then([&](websocket_incoming_message message) {
        try
        {
            cout <<message.length()<<"\n";
            auto stream = message.body();
            for (int i = 0;i<6;i++)
            {
                cout <<(char)stream.read().get();
            }
        }
        catch(const websocket_exception& ex)
        {
            std::cout << ex.what();
        }
    });
    receive_task.wait();*/

    /*
    QApplication app(argc, argv);
    QWidget window;
    window.resize(1280, 768);
    window.setWindowTitle("Trach v0.1");

    auto playerInfo = new PlayerInfoUI(&window, "Lemures64", 5);

    window.show();
    return app.exec();
    */
}