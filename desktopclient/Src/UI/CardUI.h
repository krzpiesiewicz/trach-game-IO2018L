
#ifndef TRACH_CARDUI_H
#define TRACH_CARDUI_H


#include <QtWidgets/QLabel>
#include <Src/Core/Card.h>

class CardUI : public QWidget
{


public:
    QLabel* cardImage;
    Card card;

    CardUI(QWidget* parent, Card card) : QWidget(parent), card(card)
    {
        resize(120,200);
        cardImage = new QLabel(this);
        cardImage->setPixmap(getCardImage(card.type).scaled(120, 200, Qt::KeepAspectRatio, Qt::SmoothTransformation));
    }


private:

    QPixmap getCardImage(string type)
    {
        string basePath = ":/Assets/Cards/";
        string cardName = "";
        string extension = ".jpg";
        if (type == "attack")
            cardName = "atak";
        if (type == "mass_attack")
            cardName = "atak_global";
        if (type == "defence")
            cardName = "obrona";
        if (type == "priority_inc")
            cardName = "podniesienie_priorytetu";

        QString path = QString::fromStdString(basePath + cardName + extension);
        return QPixmap(path);
    }


};


#endif //TRACH_CARDUI_H
