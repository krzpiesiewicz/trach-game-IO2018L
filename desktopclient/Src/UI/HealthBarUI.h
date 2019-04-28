
#ifndef TRACH_HEALTHBARUI_H
#define TRACH_HEALTHBARUI_H


#include <QtWidgets/QWidget>
#include <QtWidgets/QLabel>

class HealthBarUI : public QWidget
{

private:

    vector<QLabel *> hearts;

public:
    HealthBarUI(QWidget *parent): QWidget(parent) {}

    void setHealthValue(int newValue)
    {
        for (auto & heart : hearts)
        {
            delete heart;
        }
        hearts.clear();
        for (int i = 0; i < newValue; i++)
        {
            QLabel *heart = new QLabel(this);
            heart->setPixmap(
                    QPixmap(":/Assets/heart.png").scaled(20, 20, Qt::KeepAspectRatio, Qt::SmoothTransformation));
            heart->move(i * 20, 0);
            hearts.emplace_back(heart);
        }
    }
};


#endif //TRACH_HEALTHBARUI_H
