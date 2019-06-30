
#ifndef TRACH_CARDUI_H
#define TRACH_CARDUI_H


#include <QtWidgets/QLabel>
#include <Src/Core/Card.h>

/**
 * widget of a single card
 */
class CardUI : public QWidget
{


public:

    CardUI(QWidget *parent, Card card) : QWidget(parent), card(card)
    {
        resize(120, 200);
        cardImage = new QLabel(this);
        cardImage->setPixmap(getCardImage(card.type).scaled(120, 200, Qt::KeepAspectRatio, Qt::SmoothTransformation));
        show();
    }

    Card card;

private:

    QLabel *avatar;
    QLabel *cardImage;

    void addPlayerAvatar(int playingPlayerId)
    {
        avatar = new QLabel(this);
        avatar->show();
        auto avatarPath = ":/Assets/avatar" + std::to_string(1 + (playingPlayerId % 5)) + ".png";
        avatar->setPixmap(QPixmap(QString::fromStdString(avatarPath)).scaled(30, 50,
                                                                             Qt::KeepAspectRatio,
                                                                             Qt::SmoothTransformation));
    }

    QPixmap getCardImage(std::string type)
    {
        std::string basePath = ":/Assets/Cards/";
        std::string cardName = "";
        std::string extension = ".jpg";
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
